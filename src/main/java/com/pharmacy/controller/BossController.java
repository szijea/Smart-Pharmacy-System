package com.pharmacy.controller;

import com.pharmacy.dto.BossLoginRequest;
import com.pharmacy.shared.entity.Boss;
import com.pharmacy.shared.service.BossService;
import com.pharmacy.service.BossDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/boss")
public class BossController {

    // Simple in-memory token store. For production, a more robust solution like Redis or JWT would be better.
    public static final Map<String, String> activeTokens = new ConcurrentHashMap<>(); // Token -> Username

    @Autowired
    private BossService bossService;

    @Autowired
    private BossDataService bossDataService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody BossLoginRequest loginRequest) {
        if (loginRequest.getUsername() == null || loginRequest.getPassword() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Username and password are required."));
        }

        Optional<Boss> bossOpt = bossService.findByUsername(loginRequest.getUsername());
        if (bossOpt.isPresent() && bossOpt.get().getPassword().equals(loginRequest.getPassword())) {
            String token = UUID.randomUUID().toString();
            activeTokens.put(token, loginRequest.getUsername());
            return ResponseEntity.ok(Map.of("message", "Login successful", "token", token));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid credentials"));
    }

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestHeader("Authorization") String token, @RequestBody Map<String, String> payload) {
        String username = activeTokens.get(token);
        if (username == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Invalid or expired token."));
        }

        String newPassword = payload.get("newPassword");
        if (newPassword == null || newPassword.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "New password cannot be empty."));
        }

        boolean success = bossService.updatePassword(username, newPassword);
        if (success) {
            return ResponseEntity.ok(Map.of("message", "Password updated successfully."));
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Failed to update password."));
        }
    }
    
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String token) {
        if (token != null && activeTokens.containsKey(token)) {
            activeTokens.remove(token);
            return ResponseEntity.ok(Map.of("message", "Logout successful."));
        } else {
            return ResponseEntity.badRequest().body(Map.of("message", "No active session to log out from."));
        }
    }

    @GetMapping("/transfer-logs")
    public ResponseEntity<?> getAllTransferLogs(@RequestHeader("Authorization") String token) {
        // The BossAuthFilter already validates the token.
        return ResponseEntity.ok(bossDataService.getAllTransferLogs());
    }

    @GetMapping("/tenants")
    public ResponseEntity<?> getAllTenants() {
        return ResponseEntity.ok(bossDataService.getAllTenantIds());
    }

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboardData(@RequestHeader("Authorization") String token) {
        // The BossAuthFilter already validates the token.
        return ResponseEntity.ok(bossDataService.getAggregatedDashboardData());
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getAllTenantStats(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(bossDataService.getAllTenantStats());
    }

    @GetMapping("/inventory/{tenantId}")
    public ResponseEntity<?> getTenantInventory(@RequestHeader("Authorization") String token,
                                                @PathVariable String tenantId,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "10") int size,
                                                @RequestParam(required = false) String keyword) {
        return ResponseEntity.ok(bossDataService.getTenantInventory(tenantId, page, size, keyword));
    }
}
