package com.pharmacy.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/hang-orders")
@CrossOrigin(origins = "http://localhost:8080")
public class HangOrderController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllHangOrders() {
        try {
            // 返回空挂单列表
            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", Collections.emptyList());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 500);
            errorResponse.put("message", "获取挂单失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createHangOrder(@RequestBody Map<String, Object> hangOrderData) {
        try {
            // 模拟创建挂单
            String hangId = "H" + System.currentTimeMillis();

            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "挂单成功");
            response.put("hangId", hangId);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("code", 500);
            errorResponse.put("message", "挂单失败: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}