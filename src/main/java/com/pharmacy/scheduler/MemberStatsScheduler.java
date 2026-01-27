package com.pharmacy.scheduler;

import com.pharmacy.service.impl.MemberConsumptionUpdater;
import com.pharmacy.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;

/**
 * 定时任务：
 * 1. 定期清理过期会员消费缓存
 * 2. 定期批量刷新最近活跃会员的消费统计
 *
 * 原先使用 @Scheduled 注解在某些环境下因为配置文件里使用了 7 字段的 cron (包含 year)，会导致 Spring 校验失败。
 * 为了兼容 6 字段和 7 字段表达式，这里改为使用 TaskScheduler 编程式调度并在需要时对 7 字段表达式做降级（去掉 year 字段）。
 */
@Component
public class MemberStatsScheduler {

    @Autowired(required = false)
    private MemberConsumptionUpdater updater;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired(required = false)
    private TaskScheduler taskScheduler;

    // scheduled futures so we can cancel on shutdown
    private ScheduledFuture<?> evictFuture;
    private ScheduledFuture<?> batchFuture;

    // 默认 cron，如果 application.properties/yaml 中有覆盖会被读取；这里会尊重 Spring 的占位符解析（通过 env）
    @Autowired
    private org.springframework.core.env.Environment env;

    @PostConstruct
    public void init(){
        if(taskScheduler==null){
            System.out.println("[MemberStatsScheduler] 没有可用的 TaskScheduler，跳过定时任务注册");
            return;
        }
        // read cron props (fall back to defaults). Defaults chosen to 6-field Spring style.
        String evictCron = env.getProperty("member.cache.evict-cron", "0 0/20 * * * ?");
        String batchCron = env.getProperty("member.cache.batch-refresh-cron", "0 0/15 * * * ?");

        evictCron = normalizeCron(evictCron);
        batchCron = normalizeCron(batchCron);

        try{
            evictFuture = taskScheduler.schedule(this::evictExpired, new CronTrigger(evictCron));
            System.out.println("[MemberStatsScheduler] 注册 evictExpired cron="+evictCron);
        }catch(Exception e){
            System.err.println("[MemberStatsScheduler] 无法注册 evictExpired: "+e.getMessage());
        }

        try{
            batchFuture = taskScheduler.schedule(this::batchRefresh, new CronTrigger(batchCron));
            System.out.println("[MemberStatsScheduler] 注册 batchRefresh cron="+batchCron);
        }catch(Exception e){
            System.err.println("[MemberStatsScheduler] 无法注册 batchRefresh: "+e.getMessage());
        }
    }

    @PreDestroy
    public void destroy(){
        if(evictFuture!=null) evictFuture.cancel(false);
        if(batchFuture!=null) batchFuture.cancel(false);
    }

    /**
     * 将 7 字段 cron（可能包含 year）降级为 6 字段，供 Spring 使用。
     * 简单策略：如果以空白拆分得到 7 个 token，则去掉最后一个 token（通常为 year）。
     */
    private String normalizeCron(String cron){
        if(cron==null) return cron;
        String trimmed = cron.trim();
        String[] parts = trimmed.split("\\s+");
        if(parts.length==7){
            // drop the last token (year)
            StringBuilder sb = new StringBuilder();
            for(int i=0;i<6;i++){ if(i>0) sb.append(' '); sb.append(parts[i]); }
            return sb.toString();
        }
        return trimmed;
    }

    // 下面保持原有业务逻辑，改为普通方法由 taskScheduler 调用
    public void evictExpired(){
        if(updater==null) return;
        int removed = updater.evictExpired();
        if(removed>0){
            System.out.println("[MemberStatsScheduler] 过期缓存清理数量="+removed+" time="+ LocalDateTime.now());
        }
    }

    public void batchRefresh(){
        if(updater==null) return;
        try {
            Set<String> recent = updater.snapshotRecentActive(200);
            java.util.List<String> todayMembers = orderRepository.getTodayActiveMembers();
            if(todayMembers!=null){
                for(String mid: todayMembers){
                    if(mid!=null) recent.add(mid);
                    if(recent.size()>=300) break;
                }
            }
            if(!recent.isEmpty()){
                updater.refreshMembersBatch(recent);
                System.out.println("[MemberStatsScheduler] 批量刷新会员消费统计 size="+recent.size());
            }
        } catch(Exception e){
            System.err.println("[MemberStatsScheduler] 批量刷新失败:"+e.getMessage());
        }
    }
}
