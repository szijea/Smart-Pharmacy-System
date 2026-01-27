package com.pharmacy.controller;

import com.pharmacy.service.StatsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/stats")
@CrossOrigin(origins = "http://localhost:8080")
public class StatsController {

    @Autowired
    private StatsService statsService;

    // 现有接口保持不变...
    @GetMapping("/sales")
    public ResponseEntity<Map<String, Object>> getSalesStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            Map<String, Object> stats = statsService.getSalesStats(startDate, endDate);

            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "获取销售统计成功");
            response.put("data", stats);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 500);
            errorResponse.put("message", "获取销售统计失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    // 其他现有接口...

    // 新增接口：获取所有商品排行（不按时间范围）
    @GetMapping("/products/ranking/all")
    public ResponseEntity<Map<String, Object>> getAllProductRanking(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            // 注意：这里需要将 StatsServiceImpl 转换为具体实现类来调用新增的方法
            // 更好的做法是在 StatsService 接口中添加这个方法
            if (statsService instanceof com.pharmacy.service.impl.StatsServiceImpl) {
                com.pharmacy.service.impl.StatsServiceImpl statsServiceImpl =
                        (com.pharmacy.service.impl.StatsServiceImpl) statsService;

                Map<String, Object> ranking = statsServiceImpl.getAllProductRanking(limit);

                Map<String, Object> response = new HashMap<>();
                response.put("code", 200);
                response.put("message", "获取商品排行成功");
                response.put("data", ranking);

                return ResponseEntity.ok(response);
            } else {
                throw new RuntimeException("服务实现类不匹配");
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 500);
            errorResponse.put("message", "获取商品排行失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}