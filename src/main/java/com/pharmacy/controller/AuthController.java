package com.pharmacy.controller;

import com.pharmacy.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");

        if (username == null || password == null) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "用户名和密码不能为空");
            log.warn("[Auth] 缺失字段 username={} password?{}", username, password != null);
            return ResponseEntity.badRequest().body(errorResponse);
        }

        Map<String, Object> result = authService.login(username, password);
        boolean success = (Boolean) result.get("success");
        log.info("[Auth] 用户登录尝试 username='{}' success={} message='{}'", username, success, result.get("message"));

        if (success) {
            return ResponseEntity.ok(result);
        } else {
            // 使用 401 未授权更语义化
            return ResponseEntity.status(401).body(result);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout() {
        // 使用 HashMap 替代 Map.of (Java 9+ 特性)
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "登出成功");
        return ResponseEntity.ok(result);
    }
}