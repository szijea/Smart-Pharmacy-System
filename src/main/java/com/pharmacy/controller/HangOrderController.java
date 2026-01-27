package com.pharmacy.controller;

import com.pharmacy.entity.HangOrder;
import com.pharmacy.repository.HangOrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/hang-orders")
// 移除局部 @CrossOrigin，使用全局 CORS 配置
public class HangOrderController {

    @Autowired
    private HangOrderRepository hangOrderRepository;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllHangOrders() {
        try {
            List<HangOrder> list = hangOrderRepository.findAllByOrderByHangTimeDesc();
            // 转换为前端需要的格式
            List<Map<String, Object>> data = new ArrayList<>();
            for (HangOrder h : list) {
                Map<String, Object> map = new HashMap<>();
                map.put("hangId", h.getHangId());
                map.put("time", h.getHangTime());
                map.put("cart", new com.fasterxml.jackson.databind.ObjectMapper().readValue(h.getCartJson(), List.class));
                map.put("memberId", h.getMemberId());
                map.put("memberName", h.getMemberName());
                data.add(map);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", data);

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
            String hangId = "H" + System.currentTimeMillis();
            HangOrder h = new HangOrder();
            h.setHangId(hangId);
            h.setHangTime(LocalDateTime.now());
            h.setCartJson(new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(hangOrderData.get("cart")));
            h.setMemberId((String) hangOrderData.get("memberId"));
            h.setMemberName((String) hangOrderData.get("memberName"));

            hangOrderRepository.save(h);

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

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteHangOrder(@PathVariable String id) {
        try {
            hangOrderRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("code", 200, "message", "挂单已删除"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("code", 500, "message", "删除挂单失败: " + e.getMessage()));
        }
    }
}