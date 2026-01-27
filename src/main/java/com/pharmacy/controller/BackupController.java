package com.pharmacy.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/backup")
public class BackupController {

    @GetMapping("/list")
    public ResponseEntity<?> listBackups() {
        // 模拟备份列表数据
        List<Map<String, Object>> backups = new ArrayList<>();

        // 示例数据 1
        Map<String, Object> backup1 = new HashMap<>();
        backup1.put("id", "bk_20251228_001");
        backup1.put("fileName", "backup_20251228_full.sql");
        backup1.put("size", "15.2 MB");
        backup1.put("createTime", LocalDateTime.now().minusDays(1));
        backup1.put("type", "自动备份");
        backup1.put("status", "成功");
        backups.add(backup1);

        // 示例数据 2
        Map<String, Object> backup2 = new HashMap<>();
        backup2.put("id", "bk_20251221_001");
        backup2.put("fileName", "backup_20251221_full.sql");
        backup2.put("size", "14.8 MB");
        backup2.put("createTime", LocalDateTime.now().minusDays(8));
        backup2.put("type", "自动备份");
        backup2.put("status", "成功");
        backups.add(backup2);

        return ResponseEntity.ok(Map.of("code", 200, "message", "success", "data", backups));
    }

    @PostMapping("/create")
    public ResponseEntity<?> createBackup() {
        // 模拟创建备份
        return ResponseEntity.ok(Map.of("code", 200, "message", "备份任务已提交"));
    }

    @PostMapping("/restore/{id}")
    public ResponseEntity<?> restoreBackup(@PathVariable String id) {
        // 模拟恢复备份
        return ResponseEntity.ok(Map.of("code", 200, "message", "恢复任务已提交"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteBackup(@PathVariable String id) {
        // 模拟删除备份
        return ResponseEntity.ok(Map.of("code", 200, "message", "备份已删除"));
    }
}

