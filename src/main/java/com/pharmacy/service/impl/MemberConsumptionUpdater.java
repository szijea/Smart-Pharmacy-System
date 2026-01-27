package com.pharmacy.service.impl;

import com.pharmacy.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PreDestroy;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 会员消费统计增量刷新组件：避免每次接口全量聚合。
 * 若未来引入缓存，可在此写入缓存层；当前仅打印日志（聚合逻辑仍在查询端）。
 */
@Component
public class MemberConsumptionUpdater {

    @Autowired
    private OrderRepository orderRepository;

    // 替换固定线程池为可伸缩线程池，避免阻塞
    private final ExecutorService pool = Executors.newCachedThreadPool();

    // 简单内存缓存：memberId -> CacheEntry
    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private static final Duration TTL = Duration.ofMinutes(10); // 条目有效期
    @Value("${member.cache.ttl-minutes:10}")
    private int ttlMinutes;
    private final ConcurrentLinkedQueue<String> recentActiveMembers = new ConcurrentLinkedQueue<>();

    // 缓存条目结构
    private class CacheEntry {
        final int consumptionCount;
        final LocalDateTime lastConsumption;
        final long writeEpochMilli;
        CacheEntry(int count, LocalDateTime last){
            this.consumptionCount = count;
            this.lastConsumption = last;
            this.writeEpochMilli = System.currentTimeMillis();
        }
        boolean isExpired(){
            long customTTL = (ttlMinutes > 0 ? ttlMinutes : 10) * 60_000L;
            return System.currentTimeMillis() - writeEpochMilli > customTTL;
        }
    }

    /** 单会员异步刷新并更新缓存 */
    public void refreshSingleMember(String memberId){
        if(memberId==null || memberId.isBlank()) return;
        CompletableFuture.runAsync(() -> doRefreshSingle(memberId), pool)
            .exceptionally(e->{ System.err.println("[MemberStats] 异步刷新异常:"+e.getMessage()); return null;});
    }

    private void doRefreshSingle(String memberId){
        try {
            Long cnt = orderRepository.countPaidOrdersByMember(memberId);
            LocalDateTime last = orderRepository.findLastPaidOrderTime(memberId);
            int c = cnt==null?0:cnt.intValue();
            cache.put(memberId, new CacheEntry(c,last));
            recentActiveMembers.add(memberId);
            System.out.println("[MemberStats] 刷新 member="+memberId+" count="+c+" last="+last);
        } catch(Exception e){
            System.err.println("[MemberStats] 刷新失败 member="+memberId+" err="+e.getMessage());
        }
    }

    /** 批量刷新（同步执行，可由定时任务调用） */
    public void refreshMembersBatch(Set<String> memberIds){
        if(memberIds==null || memberIds.isEmpty()) return;
        try {
            // 聚合查询
            java.util.List<Object[]> agg = orderRepository.aggregateMemberConsumption(memberIds);
            Map<String,Object[]> map = agg.stream().collect(Collectors.toMap(r->(String)r[0], r->r));
            for(String mid: memberIds){
                Object[] row = map.get(mid);
                int count = 0; LocalDateTime last = null;
                if(row!=null){
                    count = ((Long)row[1]).intValue();
                    last = (LocalDateTime) row[2];
                }
                cache.put(mid, new CacheEntry(count,last));
            }
            memberIds.forEach(recentActiveMembers::add);
            System.out.println("[MemberStats] 批量刷新完成 size="+memberIds.size());
        } catch(Exception e){
            System.err.println("[MemberStats] 批量刷新失败:"+e.getMessage());
        }
    }

    /** 获取缓存值（可能为过期或不存在），过期立即返回 null 以便调用方补齐 */
    public MemberStatsSnapshot getCachedStats(String memberId){
        if(memberId==null) return null;
        CacheEntry ce = cache.get(memberId);
        if(ce==null) return null;
        if(ce.isExpired()){ cache.remove(memberId); return null; }
        return new MemberStatsSnapshot(memberId, ce.consumptionCount, ce.lastConsumption);
    }

    /** 仅用于前端拼装的快照结构 */
    public static class MemberStatsSnapshot {
        public final String memberId;
        public final int consumptionCount;
        public final LocalDateTime lastConsumptionDate;
        public MemberStatsSnapshot(String memberId, int consumptionCount, LocalDateTime last){
            this.memberId = memberId;
            this.consumptionCount = consumptionCount;
            this.lastConsumptionDate = last;
        }
    }

    /** 清理过期缓存（可由定时器调用） */
    public int evictExpired(){
        int removed = 0;
        for(Map.Entry<String,CacheEntry> e: cache.entrySet()){
            if(e.getValue().isExpired()){ cache.remove(e.getKey()); removed++; }
        }
        if(removed>0) System.out.println("[MemberStats] 过期条目清理: "+removed);
        return removed;
    }

    // 提供获取最近活跃会员集合（截取最多 200 个去重）
    public Set<String> snapshotRecentActive(int max){
        java.util.Set<String> set = new java.util.LinkedHashSet<>();
        for(String id: recentActiveMembers){
            set.add(id);
            if(set.size()>=max) break;
        }
        return set;
    }

    @PreDestroy
    public void shutdown(){
        pool.shutdown();
        try {
            if (!pool.awaitTermination(3, TimeUnit.SECONDS)) {
                pool.shutdownNow();
            }
        } catch (InterruptedException ignored) {}
    }
}
