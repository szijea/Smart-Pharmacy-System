package com.pharmacy.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class AutoSchemaPatch implements ApplicationRunner {
    private final JdbcTemplate jdbcTemplate;
    public AutoSchemaPatch(JdbcTemplate jdbcTemplate){ this.jdbcTemplate = jdbcTemplate; }

    @Override
    public void run(ApplicationArguments args) {
        System.out.println("[AUTO SCHEMA PATCH] 开始检测 medicine 表缺失列...");
        patchColumn("status", "VARCHAR(20) NULL COMMENT '销售状态'", "UPDATE medicine SET status='ACTIVE' WHERE status IS NULL");
        patchColumn("barcode", "VARCHAR(64) NULL COMMENT '条形码'", null);
        patchColumn("production_date", "DATE NULL COMMENT '生产日期'", null);
        patchColumn("expiry_date", "DATE NULL COMMENT '到期日期'", null);
        patchColumn("deleted", "TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标记'", null);
        patchColumn("contraindication", "VARCHAR(255) NULL COMMENT '禁忌症'", null);
        patchColumn("usage_dosage", "VARCHAR(255) NULL COMMENT '用法用量'", null);
        System.out.println("[AUTO SCHEMA PATCH] 检测完成。");
    }

    private void patchColumn(String column, String definition, String postSql){
        try {
            Integer cnt = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME='medicine' AND COLUMN_NAME=?",
                    Integer.class, column);
            if(cnt != null && cnt == 0){
                System.out.println("[AUTO SCHEMA PATCH] 添加列: " + column);
                jdbcTemplate.execute("ALTER TABLE medicine ADD COLUMN " + column + " " + definition);
                if(postSql != null){ jdbcTemplate.execute(postSql); }
            } else {
                System.out.println("[AUTO SCHEMA PATCH] 已存在列: " + column);
            }
        } catch (Exception e){
            System.out.println("[AUTO SCHEMA PATCH] 处理列 " + column + " 失败: " + e.getMessage());
        }
    }
}
