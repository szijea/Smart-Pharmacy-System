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
        patchColumn("medicine", "status", "VARCHAR(20) NULL COMMENT '销售状态'", "UPDATE medicine SET status='ACTIVE' WHERE status IS NULL");
        patchColumn("medicine", "barcode", "VARCHAR(64) NULL COMMENT '条形码'", null);
        patchColumn("medicine", "production_date", "DATE NULL COMMENT '生产日期'", null);
        patchColumn("medicine", "expiry_date", "DATE NULL COMMENT '到期日期'", null);
        patchColumn("medicine", "deleted", "TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标记'", null);
        patchColumn("medicine", "contraindication", "VARCHAR(255) NULL COMMENT '禁忌症'", null);
        patchColumn("medicine", "usage_dosage", "VARCHAR(255) NULL COMMENT '用法用量'", null);

        System.out.println("[AUTO SCHEMA PATCH] 开始检测 system_settings 表缺失列...");
        if (tableExists("system_settings")) {
            patchColumn("system_settings", "store_name", "VARCHAR(255) NULL", null);
            patchColumn("system_settings", "store_phone", "VARCHAR(50) NULL", null);
            patchColumn("system_settings", "store_address", "VARCHAR(255) NULL", null);
            patchColumn("system_settings", "store_desc", "VARCHAR(255) NULL", null);
            patchColumn("system_settings", "low_stock_threshold", "INT NULL", null);
            patchColumn("system_settings", "notify_methods", "VARCHAR(255) NULL", null);
            patchColumn("system_settings", "points_rule", "DOUBLE NULL", null);
            patchColumn("system_settings", "cash_rule", "INT NULL", null);
            patchColumn("system_settings", "operation_log", "TINYINT(1) NULL", null);
            patchColumn("system_settings", "created_at", "DATETIME NULL", null);
            patchColumn("system_settings", "updated_at", "DATETIME NULL", null);
            patchColumn("system_settings", "open_time", "VARCHAR(10) NULL", null);
            patchColumn("system_settings", "close_time", "VARCHAR(10) NULL", null);
            patchColumn("system_settings", "license_number", "VARCHAR(64) NULL", null);
            patchColumn("system_settings", "notify_system", "TINYINT(1) NULL", null);
            patchColumn("system_settings", "notify_email", "TINYINT(1) NULL", null);
            patchColumn("system_settings", "notify_sms", "TINYINT(1) NULL", null);
            patchColumn("system_settings", "notify_email_address", "VARCHAR(128) NULL", null);
            patchColumn("system_settings", "email_frequency", "VARCHAR(20) NULL", null);
            patchColumn("system_settings", "notify_phone", "VARCHAR(20) NULL", null);
            patchColumn("system_settings", "sms_frequency", "VARCHAR(20) NULL", null);
            patchColumn("system_settings", "inventory_check_cycle", "VARCHAR(20) NULL", null);
            patchColumn("system_settings", "normal_to_silver", "INT NULL", null);
            patchColumn("system_settings", "normal_discount", "INT NULL", null);
            patchColumn("system_settings", "silver_to_gold", "INT NULL", null);
            patchColumn("system_settings", "silver_discount", "INT NULL", null);
            patchColumn("system_settings", "gold_to_platinum", "INT NULL", null);
            patchColumn("system_settings", "gold_discount", "INT NULL", null);
            patchColumn("system_settings", "enable_cash", "TINYINT(1) NULL", null);
            patchColumn("system_settings", "enable_wechat", "TINYINT(1) NULL", null);
            patchColumn("system_settings", "enable_alipay", "TINYINT(1) NULL", null);
            patchColumn("system_settings", "enable_member_card", "TINYINT(1) NULL", null);
            patchColumn("system_settings", "wechat_mch_id", "VARCHAR(64) NULL", null);
            patchColumn("system_settings", "wechat_api_key", "VARCHAR(128) NULL", null);
            patchColumn("system_settings", "alipay_app_id", "VARCHAR(64) NULL", null);
            patchColumn("system_settings", "alipay_private_key", "TEXT NULL", null);
            patchColumn("system_settings", "change_unit", "VARCHAR(10) NULL", null);
            patchColumn("system_settings", "default_printer", "VARCHAR(32) NULL", null);
            patchColumn("system_settings", "paper_size", "VARCHAR(32) NULL", null);
            patchColumn("system_settings", "print_logo", "TINYINT(1) NULL", null);
            patchColumn("system_settings", "print_qrcode", "TINYINT(1) NULL", null);
            patchColumn("system_settings", "print_member", "TINYINT(1) NULL", null);
            patchColumn("system_settings", "print_drug_detail", "TINYINT(1) NULL", null);
            patchColumn("system_settings", "print_usage", "TINYINT(1) NULL", null);
            patchColumn("system_settings", "print_footer", "TINYINT(1) NULL", null);
            patchColumn("system_settings", "footer_text", "VARCHAR(512) NULL", null);
            patchColumn("system_settings", "receipt_copies", "INT NULL", null);
            patchColumn("system_settings", "prescription_copies", "INT NULL", null);
            patchColumn("system_settings", "enable_log", "TINYINT(1) NULL", null);
            patchColumn("system_settings", "log_retention", "INT NULL", null);
            patchColumn("system_settings", "pwd_expiry", "INT NULL", null);
            patchColumn("system_settings", "login_attempts", "INT NULL", null);
            patchColumn("system_settings", "enable_ip_restrict", "TINYINT(1) NULL", null);
            patchColumn("system_settings", "auto_backup", "TINYINT(1) NULL", null);
            patchColumn("system_settings", "backup_cycle", "VARCHAR(20) NULL", null);
            patchColumn("system_settings", "backup_retention", "INT NULL", null);
        } else {
            System.out.println("[AUTO SCHEMA PATCH] system_settings 不存在，跳过补列。");
        }
        System.out.println("[AUTO SCHEMA PATCH] 检测完成。");
    }

    private void patchColumn(String table, String column, String definition, String postSql){
        try {
            Integer cnt = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME=? AND COLUMN_NAME=?",
                    Integer.class, table, column);
            if(cnt != null && cnt == 0){
                System.out.println("[AUTO SCHEMA PATCH] 添加列: " + table + "." + column);
                jdbcTemplate.execute("ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition);
                if(postSql != null){ jdbcTemplate.execute(postSql); }
            } else {
                System.out.println("[AUTO SCHEMA PATCH] 已存在列: " + table + "." + column);
            }
        } catch (Exception e){
            System.out.println("[AUTO SCHEMA PATCH] 处理列 " + table + "." + column + " 失败: " + e.getMessage());
        }
    }

    private boolean tableExists(String table){
        try {
            Integer cnt = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME=?",
                    Integer.class, table);
            return cnt != null && cnt > 0;
        } catch (Exception e){
            System.out.println("[AUTO SCHEMA PATCH] 检测表失败: " + table + ", " + e.getMessage());
            return false;
        }
    }
}
