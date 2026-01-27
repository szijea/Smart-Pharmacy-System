package com.pharmacy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();

        try {
            // 检查数据库连接
            jdbcTemplate.execute("SELECT 1");
            health.put("database", "UP");

            // 检查表是否存在
            checkTables(health);

            health.put("status", "UP");
            health.put("code", 200);
            health.put("message", "系统运行正常");

            return ResponseEntity.ok(health);

        } catch (Exception e) {
            health.put("status", "DOWN");
            health.put("code", 503);
            health.put("message", "系统异常: " + e.getMessage());
            health.put("database", "DOWN");

            return ResponseEntity.status(503).body(health);
        }
    }

    private void checkTables(Map<String, Object> health) {
        try {
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM orders", Integer.class);
            health.put("orders_table", "EXISTS");
        } catch (Exception e) {
            health.put("orders_table", "MISSING");
        }

        try {
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM order_items", Integer.class);
            health.put("order_items_table", "EXISTS");
        } catch (Exception e) {
            health.put("order_items_table", "MISSING");
        }
    }
}