package com.pharmacy.config;

import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SchemaVerifier {
    private final JdbcTemplate jdbcTemplate;
    public SchemaVerifier(JdbcTemplate jdbcTemplate){ this.jdbcTemplate = jdbcTemplate; }

    @PostConstruct
    public void verify() {
        try {
            List<String> missing = new java.util.ArrayList<>();
            checkColumn("medicine","status", missing);
            checkColumn("medicine","barcode", missing);
            checkColumn("medicine","production_date", missing);
            checkColumn("medicine","expiry_date", missing);
            if(!missing.isEmpty()) {
                System.out.println("[SCHEMA WARNING] 缺失列: " + String.join(",", missing) + " 。请运行最新 Flyway 迁移或手动添加。");
            } else {
                System.out.println("[SCHEMA OK] 所有药品相关列已存在。");
            }
        } catch (Exception e){
            System.out.println("[SCHEMA CHECK ERROR] " + e.getMessage());
        }
    }

    private void checkColumn(String table, String column, List<String> missing){
        Integer cnt = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME=? AND COLUMN_NAME=?",
                Integer.class, table, column);
        if(cnt == null || cnt == 0){ missing.add(table+"."+column); }
    }
}

