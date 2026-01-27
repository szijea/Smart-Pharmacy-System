package com.pharmacy.controller;

import com.pharmacy.dto.MemberDTO;
import com.pharmacy.dto.MemberStatsDTO;
import com.pharmacy.entity.Member;
import com.pharmacy.repository.OrderRepository;
import com.pharmacy.service.MemberService;
import com.pharmacy.service.impl.MemberConsumptionUpdater;
import com.pharmacy.multitenant.TenantContext; // 新增导入
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.ArrayList; // 添加这个导入
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/members")
// 使用全局 CORS 配置
public class MemberController {

    @Autowired
    private MemberService memberService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired(required = false)
    private MemberConsumptionUpdater memberConsumptionUpdater;

    @Autowired
    private com.pharmacy.repository.PointRewardRepository pointRewardRepository;

    @Autowired
    private com.pharmacy.repository.MemberRepository memberRepository;

    // 会员搜索端点
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchMembers(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            String tenant = TenantContext.getTenant();
            System.out.println("=== 会员搜索请求 === 租户=" + tenant);
            System.out.println("keyword: " + keyword);
            System.out.println("page: " + page);
            System.out.println("size: " + size);

            // 处理空关键词
            if (keyword == null || keyword.trim().isEmpty()) {
                System.out.println("关键词为空，返回提示信息");
                Map<String, Object> response = new HashMap<>();
                response.put("code", 200);
                response.put("message", "请输入搜索关键词（姓名、手机号或卡号）");
                response.put("data", new ArrayList<>());
                response.put("total", 0);
                response.put("currentPage", page);
                response.put("totalPages", 0);
                return ResponseEntity.ok(response);
            }

            // 调用会员搜索服务
            List<Member> members = memberService.searchMembers(keyword.trim());

            // 简单的分页逻辑
            int total = members.size();
            int start = Math.min((page - 1) * size, total);
            int end = Math.min(start + size, total);
            List<Member> pagedMembers = (start < end) ? members.subList(start, end) : new ArrayList<>();

            // 转换为DTO并填充消费统计
            List<MemberDTO> dtoList = enrichMembersWithConsumption(pagedMembers);

            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", dtoList);
            response.put("total", total);
            response.put("currentPage", page);
            response.put("totalPages", (int) Math.ceil((double) total / size));

            System.out.println("搜索完成，返回 " + dtoList.size() + " 个结果");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("会员搜索出错: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "会员搜索失败: " + e.getMessage());
            response.put("errorDetails", e.toString());
            response.put("data", new ArrayList<>());

            return ResponseEntity.internalServerError().body(response);
        }
    }

    // 测试搜索端点
    @GetMapping("/test-search")
    public ResponseEntity<Map<String, Object>> testSearch(@RequestParam String keyword) {
        try {
            System.out.println("=== 测试会员搜索 ===");
            System.out.println("测试关键词: " + keyword);

            List<Member> members = memberService.searchMembers(keyword);

            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "搜索测试成功");
            response.put("keyword", keyword);
            response.put("results", members);
            response.put("count", members.size());

            // 打印详细信息
            System.out.println("搜索到 " + members.size() + " 个会员:");
            members.forEach(member -> {
                System.out.println(" - " + member.getName() + " (手机: " + member.getPhone() + ")");
            });

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("测试搜索失败: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "测试搜索失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // 健康检查端点
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "Member Service");
        response.put("timestamp", java.time.LocalDateTime.now().toString());
        return ResponseEntity.ok(response);
    }

    // 测试端点
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Member Service is working! - " + java.time.LocalDateTime.now());
    }

    // 数据库测试端点
    @GetMapping("/test-db")
    public ResponseEntity<Map<String, Object>> testDatabase() {
        try {
            // 测试数据库连接和基本操作
            long count = memberService.findAll().size();

            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "数据库连接正常");
            response.put("totalMembers", count);
            response.put("timestamp", java.time.LocalDateTime.now().toString());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("数据库测试失败: " + e.getMessage());
            e.printStackTrace();

            Map<String, Object> response = new HashMap<>();
            response.put("code", 500);
            response.put("message", "数据库连接失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    // 其他会员相关方法...
    @GetMapping
    public List<MemberDTO> getAllMembers() {
        List<Member> members = memberService.findAll();
        return enrichMembersWithConsumption(members);
    }

    @GetMapping("/{memberId}")
    public ResponseEntity<Member> getMemberById(@PathVariable String memberId) {
        Optional<Member> member = memberService.findById(memberId);
        return member.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/phone/{phone}")
    public ResponseEntity<Member> getMemberByPhone(@PathVariable String phone) {
        Optional<Member> member = memberService.findByPhone(phone);
        return member.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createMember(@RequestBody Member member) {
        try {
            // 如果前端没有提供memberId，自动生成
            if (member.getMemberId() == null || member.getMemberId().trim().isEmpty()) {
                member.setMemberId(memberService.generateNextMemberId());
            }

            Member savedMember = memberService.createMember(
                    member.getMemberId(),
                    member.getName(),
                    member.getPhone()
            );

            // 设置其他可选字段
            if (member.getCardNo() != null) {
                savedMember.setCardNo(member.getCardNo());
            }
            if (member.getAllergicHistory() != null) {
                savedMember.setAllergicHistory(member.getAllergicHistory());
            }
            if (member.getMedicalCardNo() != null) {
                savedMember.setMedicalCardNo(member.getMedicalCardNo());
            }

            return ResponseEntity.ok(savedMember);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{memberId}")
    public ResponseEntity<?> updateMember(@PathVariable String memberId, @RequestBody Member member) {
        try {
            if (!memberId.equals(member.getMemberId())) {
                return ResponseEntity.badRequest().body("会员ID不匹配");
            }

            Member updatedMember = memberService.updateMember(member);
            return ResponseEntity.ok(updatedMember);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMember(@PathVariable String id) {
        memberService.deleteMember(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/exchange")
    public ResponseEntity<?> exchangeReward(@PathVariable String id, @RequestBody Map<String, Integer> body) {
        Integer points = body.get("points");
        if (points == null || points <= 0) {
            return ResponseEntity.badRequest().body("无效的积分值");
        }
        try {
            memberService.exchangeReward(id, points);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/batch-delete")
    public ResponseEntity<?> batchDeleteMembers(@RequestBody List<String> memberIds) {
        try {
            boolean success = memberService.deleteMembers(memberIds);
            if (success) {
                return ResponseEntity.ok().body("删除成功");
            } else {
                return ResponseEntity.badRequest().body("删除失败");
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("删除失败: " + e.getMessage());
        }
    }

    // 获取会员统计数据
    @GetMapping("/stats")
    public ResponseEntity<MemberStatsDTO> getMemberStats() {
        try {
            MemberStatsDTO stats = memberService.getMemberStats();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            System.err.println("获取会员统计失败: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // 筛选会员
    @GetMapping("/filter")
    public ResponseEntity<List<Member>> filterMembers(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) Integer level,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        try {
            LocalDateTime start = startDate != null ? LocalDateTime.parse(startDate) : null;
            LocalDateTime end = endDate != null ? LocalDateTime.parse(endDate) : null;

            List<Member> members = memberService.filterMembers(name, phone, level, start, end);
            return ResponseEntity.ok(members);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // 获取所有会员（分页版本）
    @GetMapping("/page")
    public ResponseEntity<Map<String, Object>> getMembersPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        try {
            List<Member> allMembers = memberService.findAll();
            int total = allMembers.size();
            int start = Math.min(page * size, total);
            int end = Math.min(start + size, total);
            List<Member> pageMembers = start < end ? allMembers.subList(start, end) : java.util.Collections.emptyList();
            List<MemberDTO> dtoPage = enrichMembersWithConsumption(pageMembers);

            Map<String, Object> response = new HashMap<>();
            response.put("data", dtoPage);
            response.put("currentPage", page);
            response.put("totalItems", total);
            response.put("totalPages", (int) Math.ceil((double) total / size));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // 快速搜索端点
    @GetMapping("/quick-search")
    public ResponseEntity<Map<String, Object>> quickSearch(@RequestParam String keyword) {
        String tenant = TenantContext.getTenant();
        System.out.println("=== 快速会员搜索 === 租户=" + tenant + " keyword=" + keyword);
        List<Member> members = memberService.quickSearch(keyword);
        List<MemberDTO> dto = enrichMembersWithConsumption(members);
        return ResponseEntity.ok(Map.of(
                "code", 200,
                "message", "success",
                "tenant", tenant,
                "data", dto,
                "total", dto.size()
        ));
    }

    // 调试端点：获取当前租户的所有会员及其原始名称
    @GetMapping("/debug-all")
    public ResponseEntity<Map<String,Object>> debugAll(){
        String tenant = TenantContext.getTenant();
        List<Member> all = memberService.findAll();
        List<Map<String,Object>> raw = new ArrayList<>();
        for(Member m: all){
            Map<String,Object> row = new HashMap<>();
            row.put("memberId", m.getMemberId());
            row.put("name", m.getName());
            row.put("hexName", m.getName()!=null? toHex(m.getName()): null);
            row.put("phone", m.getPhone());
            row.put("cardNo", m.getCardNo());
            raw.add(row);
        }
        return ResponseEntity.ok(Map.of(
                "tenant", tenant,
                "count", all.size(),
                "data", raw
        ));
    }
    private String toHex(String s){
        StringBuilder sb=new StringBuilder();
        for(char c: s.toCharArray()) sb.append(Integer.toHexString(c)).append(' ');
        return sb.toString().trim();
    }

    private List<MemberDTO> enrichMembersWithConsumption(List<Member> members){
        if(members==null || members.isEmpty()) return java.util.Collections.emptyList();
        java.util.Map<String, MemberDTO> map = new java.util.LinkedHashMap<>();
        java.util.Set<String> missingForBatch = new java.util.HashSet<>();
        for(Member m: members){
            MemberDTO dto = new MemberDTO(m.getMemberId(), m.getName(), m.getPhone());
            dto.setCardNo(m.getCardNo());
            dto.setLevel(m.getLevel());
            dto.setPoints(m.getPoints());
            dto.setAllergicHistory(m.getAllergicHistory());
            dto.setMedicalCardNo(m.getMedicalCardNo());
            dto.setCreateTime(m.getCreateTime());
            dto.setLevelName(MemberDTO.getLevelName(m.getLevel()));
            map.put(m.getMemberId(), dto);
        }
        // 优先从缓存取值
        if(memberConsumptionUpdater!=null){
            for(String mid: map.keySet()){
                MemberConsumptionUpdater.MemberStatsSnapshot snap = memberConsumptionUpdater.getCachedStats(mid);
                if(snap!=null){
                    MemberDTO dto = map.get(mid);
                    dto.setConsumptionCount(snap.consumptionCount);
                    dto.setLastConsumptionDate(snap.lastConsumptionDate);
                } else {
                    missingForBatch.add(mid);
                }
            }
            // 批量数据库聚合填充缺失项并更新缓存
            if(!missingForBatch.isEmpty()){
                try {
                    memberConsumptionUpdater.refreshMembersBatch(missingForBatch); // 刷新缓存
                    for(String mid: missingForBatch){
                        MemberConsumptionUpdater.MemberStatsSnapshot snap2 = memberConsumptionUpdater.getCachedStats(mid);
                        if(snap2!=null){
                            MemberDTO dto = map.get(mid);
                            dto.setConsumptionCount(snap2.consumptionCount);
                            dto.setLastConsumptionDate(snap2.lastConsumptionDate);
                        } else {
                            // 缓存仍无，置默认值
                            MemberDTO dto = map.get(mid);
                            dto.setConsumptionCount(0);
                        }
                    }
                } catch(Exception e){
                    System.err.println("批量填充会员消费统计失败: "+e.getMessage());
                    // 回退：缺失全部置0
                    for(String mid: missingForBatch){
                        MemberDTO dto = map.get(mid);
                        if(dto.getConsumptionCount()==null) dto.setConsumptionCount(0);
                    }
                }
            }
        } else {
            // 无缓存组件时原始聚合逻辑
            java.util.List<Object[]> agg = orderRepository.aggregateMemberConsumption(map.keySet());
            for(Object[] row: agg){
                String memberId = (String) row[0];
                Long count = (Long) row[1];
                java.time.LocalDateTime last = (java.time.LocalDateTime) row[2];
                MemberDTO dto = map.get(memberId);
                if(dto!=null){
                    dto.setConsumptionCount(count!=null? count.intValue():0);
                    dto.setLastConsumptionDate(last);
                }
            }
            for(MemberDTO dto: map.values()){
                if(dto.getConsumptionCount()==null) dto.setConsumptionCount(0);
            }
        }
        return new java.util.ArrayList<>(map.values());
    }

    // 高级筛选 + 分页 + 排序端点
    @GetMapping("/advanced")
    public ResponseEntity<Map<String,Object>> advancedMembers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String segment,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Integer pointsMin,
            @RequestParam(required = false) Integer pointsMax,
            @RequestParam(required = false) Integer consumptionMin,
            @RequestParam(required = false) Integer consumptionMax,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String phone
    ) {
        Map<String,Object> res = new HashMap<>();
        try {
            if(page < 1) page = 1; if(size < 1) size = 10; if(size > 200) size = 200; // 简单约束
            List<MemberDTO> all = enrichMembersWithConsumption(memberService.findAll());
            long nowMillis = System.currentTimeMillis();
            long activeThreshold = nowMillis - 30L*24*3600*1000; // 最近30天
            long sleepThreshold  = nowMillis - 90L*24*3600*1000; // 超过90天视为沉睡
            // 基础过滤
            List<MemberDTO> filtered = new ArrayList<>();
            for(MemberDTO m : all){
                if(name != null && !name.isBlank() && (m.getName()==null || !m.getName().contains(name.trim()))) continue;
                if(phone != null && !phone.isBlank() && (m.getPhone()==null || !m.getPhone().contains(phone.trim()))) continue;
                if(pointsMin != null && (m.getPoints()==null || m.getPoints() < pointsMin)) continue;
                if(pointsMax != null && (m.getPoints()==null || m.getPoints() > pointsMax)) continue;
                if(consumptionMin != null && (m.getConsumptionCount()==null || m.getConsumptionCount() < consumptionMin)) continue;
                if(consumptionMax != null && (m.getConsumptionCount()==null || m.getConsumptionCount() > consumptionMax)) continue;
                // segment 分组筛选
                if(segment != null && !segment.isBlank()){
                    boolean keep = true;
                    switch(segment){
                        case "vip": keep = m.getLevel()!=null && m.getLevel() >= 4; break;
                        case "new30": keep = m.getCreateTime()!=null && m.getCreateTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli() >= activeThreshold; break;
                        case "sleep": {
                            java.time.LocalDateTime last = m.getLastConsumptionDate();
                            long lastMs = (last==null)?0:last.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
                            keep = lastMs == 0 || lastMs < sleepThreshold; break; }
                        case "highPoints": keep = m.getPoints()!=null && m.getPoints() >= 1000; break;
                        default: break; // all
                    }
                    if(!keep) continue;
                }
                filtered.add(m);
            }
            // 排序
            if(sort != null && !sort.isBlank()){
                String[] parts = sort.split("_");
                if(parts.length == 2){
                    String field = parts[0]; String dir = parts[1];
                    java.util.Comparator<MemberDTO> cmp = java.util.Comparator.comparingInt(a->0); // 默认
                    switch(field){
                        case "createTime": cmp = java.util.Comparator.comparing(a-> a.getCreateTime()==null? java.time.LocalDateTime.MIN : a.getCreateTime()); break;
                        case "points": cmp = java.util.Comparator.comparing(a-> a.getPoints()==null?0:a.getPoints()); break;
                        case "consumption": cmp = java.util.Comparator.comparing(a-> a.getConsumptionCount()==null?0:a.getConsumptionCount()); break;
                        case "level": cmp = java.util.Comparator.comparing(a-> a.getLevel()==null?0:a.getLevel()); break;
                    }
                    if("desc".equalsIgnoreCase(dir)) cmp = cmp.reversed();
                    filtered.sort(cmp);
                }
            }
            // 全局聚合指标（基于筛选后的完整集合，不是当前页）
            int total = filtered.size();
            double avgPoints = 0.0; int activeCount = 0; int churnCount = 0; long now = System.currentTimeMillis();
            if(total > 0){
                for(MemberDTO m: filtered){
                    avgPoints += (m.getPoints()==null?0:m.getPoints());
                    Long lastMs = null;
                    if(m.getLastConsumptionDate()!=null){
                        lastMs = m.getLastConsumptionDate().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
                    }
                    if(lastMs!=null && (now - lastMs) < 30L*24*3600*1000) activeCount++;
                    if(lastMs==null || (now - lastMs) > 90L*24*3600*1000) churnCount++;
                }
                avgPoints = avgPoints / total;
            }
            double activeRate = total==0?0.0: (activeCount * 100.0 / total);
            // 分页切片
            int start = (page-1)*size;
            if(start > total) start = total;
            int end = Math.min(start + size, total);
            List<MemberDTO> pageList = filtered.subList(start, end);
            res.put("code", 200);
            res.put("message", "success");
            res.put("currentPage", page);
            res.put("pageSize", size);
            res.put("totalItems", total);
            res.put("totalPages", (int)Math.ceil(total/(double)size));
            res.put("data", pageList);
            res.put("avgPoints", Math.round(avgPoints));
            res.put("activeRate", String.format(java.util.Locale.ROOT, "%.1f%%", activeRate));
            res.put("churnCount", churnCount);
            return ResponseEntity.ok(res);
        } catch(Exception e){
            res.put("code", 500);
            res.put("message", "advanced query failed: "+e.getMessage());
            res.put("error", e.toString());
            return ResponseEntity.internalServerError().body(res);
        }
    }

    // 导出会员
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportMembers() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Members");
            Row header = sheet.createRow(0);
            String[] columns = {"ID", "姓名", "手机号", "会员卡号", "等级", "积分", "过敏史", "医保卡号", "注册时间"};
            for (int i = 0; i < columns.length; i++) {
                header.createCell(i).setCellValue(columns[i]);
            }

            List<Member> members = memberService.findAll();
            int rowNum = 1;
            for ( Member member : members) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(member.getMemberId());
                row.createCell(1).setCellValue(member.getName());
                row.createCell(2).setCellValue(member.getPhone());
                row.createCell(3).setCellValue(member.getCardNo());
                row.createCell(4).setCellValue(member.getLevel() != null ? member.getLevel() : 0);
                row.createCell(5).setCellValue(member.getPoints() != null ? member.getPoints() : 0);
                row.createCell(6).setCellValue(member.getAllergicHistory());
                row.createCell(7).setCellValue(member.getMedicalCardNo());
                row.createCell(8).setCellValue(member.getCreateTime() != null ? member.getCreateTime().toString() : "");
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "members.xlsx");

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(out.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // 导入会员
    @PostMapping("/import")
    public ResponseEntity<?> importMembers(@RequestParam("file") MultipartFile file) {
        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            List<String> errors = new ArrayList<>();
            int successCount = 0;

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header

                try {
                    String name = getCellValue(row.getCell(1));
                    String phone = getCellValue(row.getCell(2));

                    if (name == null || name.isEmpty() || phone == null || phone.isEmpty()) {
                        continue; // Skip invalid rows
                    }

                    Member member = new Member();
                    member.setMemberId(memberService.generateNextMemberId());
                    member.setName(name);
                    member.setPhone(phone);
                    member.setCardNo(getCellValue(row.getCell(3)));

                    String levelStr = getCellValue(row.getCell(4));
                    if (levelStr != null && !levelStr.isEmpty()) {
                        try {
                            member.setLevel((int) Double.parseDouble(levelStr));
                        } catch (NumberFormatException e) {
                            member.setLevel(0);
                        }
                    } else {
                        member.setLevel(0);
                    }

                    String pointsStr = getCellValue(row.getCell(5));
                    if (pointsStr != null && !pointsStr.isEmpty()) {
                        try {
                            member.setPoints((int) Double.parseDouble(pointsStr));
                        } catch (NumberFormatException e) {
                            member.setPoints(0);
                        }
                    } else {
                        member.setPoints(0);
                    }

                    member.setAllergicHistory(getCellValue(row.getCell(6)));
                    member.setMedicalCardNo(getCellValue(row.getCell(7)));

                    // Check if phone exists
                    if (memberService.findByPhone(phone).isPresent()) {
                        errors.add("Row " + (row.getRowNum() + 1) + ": Phone " + phone + " already exists.");
                        continue;
                    }

                    memberService.createMember(member.getMemberId(), member.getName(), member.getPhone());
                    // Update other fields that createMember might not handle if it only takes basic info
                    // Re-fetch or update
                    Member saved = memberService.findById(member.getMemberId()).orElse(null);
                    if(saved != null) {
                        saved.setCardNo(member.getCardNo());
                        saved.setLevel(member.getLevel());
                        saved.setPoints(member.getPoints());
                        saved.setAllergicHistory(member.getAllergicHistory());
                        saved.setMedicalCardNo(member.getMedicalCardNo());
                        memberService.updateMember(saved);
                    }

                    successCount++;
                } catch (Exception e) {
                    errors.add("Row " + (row.getRowNum() + 1) + ": " + e.getMessage());
                }
            }

            Map<String, Object> response = new HashMap<>();
            response.put("success", successCount);
            response.put("errors", errors);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Import failed: " + e.getMessage());
        }
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return null;
        switch (cell.getCellType()) {
            case STRING: return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                }
                // Avoid scientific notation for phone numbers
                return new java.text.DecimalFormat("#").format(cell.getNumericCellValue());
            case BOOLEAN: return String.valueOf(cell.getBooleanCellValue());
            default: return "";
        }
    }

    @PostMapping("/{memberId}/redeem")
    @org.springframework.transaction.annotation.Transactional
    public ResponseEntity<?> redeemReward(@PathVariable String memberId, @RequestBody Map<String, Object> payload) {
        try {
            Object rewardIdObj = payload.get("rewardId");
            if(rewardIdObj == null){
                return ResponseEntity.badRequest().body(Map.of("message", "无效的请求参数：rewardId 缺失"));
            }
            Long rewardId = Long.valueOf(rewardIdObj.toString());
            Optional<Member> memberOpt = memberRepository.findById(memberId);
            if (memberOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            Member member = memberOpt.get();

            Optional<com.pharmacy.entity.PointReward> rewardOpt = pointRewardRepository.findById(rewardId);
            if (rewardOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "福利不存在"));
            }
            com.pharmacy.entity.PointReward reward = rewardOpt.get();

            if (!Boolean.TRUE.equals(reward.getIsActive())) {
                 return ResponseEntity.badRequest().body(Map.of("message", "该福利已下架"));
            }

            if (member.getPoints() < reward.getPointsRequired()) {
                return ResponseEntity.badRequest().body(Map.of("message", "积分不足"));
            }

            // Deduct points
            member.setPoints(member.getPoints() - reward.getPointsRequired());
            memberRepository.save(member);

            // Here you might want to create a RedemptionRecord

            return ResponseEntity.ok(Map.of("message", "兑换成功", "remainingPoints", member.getPoints()));
        } catch (Exception e) {
             return ResponseEntity.internalServerError().body(Map.of("message", "兑换失败：" + e.getMessage()));
        }
    }

    @GetMapping("/init-test-data")
    public ResponseEntity<?> initTestData() {
        try {
            if (memberService.findAll().isEmpty()) {
                String tenant = TenantContext.getTenant();
                System.out.println("Initializing test data for tenant: " + tenant);
                
                memberService.createMember("M001", "张三", "13800138000");
                Member m1 = memberService.findById("M001").orElseThrow();
                m1.setLevel(1); m1.setPoints(500); memberService.updateMember(m1);

                memberService.createMember("M002", "李四", "13900139000");
                Member m2 = memberService.findById("M002").orElseThrow();
                m2.setLevel(2); m2.setPoints(1200); memberService.updateMember(m2);

                memberService.createMember("M003", "王五", "13700137000");
                Member m3 = memberService.findById("M003").orElseThrow();
                m3.setLevel(4); m3.setPoints(5000); memberService.updateMember(m3);
                
                return ResponseEntity.ok("Initialized 3 members for " + (tenant==null?"default":tenant));
            }
            return ResponseEntity.ok("Data already exists");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Init failed: " + e.getMessage());
        }
    }
}

