package com.pharmacy.multitenant;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.Ordered;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

@Configuration
public class MultiTenantSchemaInitializer {

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public ApplicationRunner sharedTableInitializer(@Qualifier("defaultDataSource") DataSource defaultDataSource) {
        return args -> {
            try (Connection conn = defaultDataSource.getConnection()) {
                String catalog = conn.getCatalog();
                ensureBossTable(conn, catalog);
                ensureProductTransferLogTable(conn, catalog);
            } catch (Exception ex) {
                System.err.println("[SchemaInit] Failed to init shared tables: " + ex.getMessage());
            }
        };
    }

    @Bean
    @Order(1)
    public ApplicationRunner stockInTablesInitializer(@Qualifier("tenantDataSources") Map<String, DataSource> tenantDataSources) {
        return args -> {
            System.out.println("[SchemaInit] 开始检测并补齐各租户的入库/供应商相关表...");
            // 先对每个租户确保关键表
            for (String tenantId : tenantDataSources.keySet()) {
                DataSource ds = tenantDataSources.get(tenantId);
                try (Connection conn = ds.getConnection()) {
                    String catalog = conn.getCatalog();
                    System.out.println("[SchemaInit] 租户=" + tenantId + " 库=" + catalog);
                    // 新增：保证核心业务表存在（避免 member / category 不存在导致 1146 错误）
                    ensureCategoryTable(conn, catalog);
                    ensureRoleTable(conn, catalog);
                    ensureMemberTable(conn, catalog);
                    ensureEmployeeTable(conn, catalog);
                    ensureMedicineTable(conn, catalog);
                    ensureInventoryTable(conn, catalog);
                    ensureSystemSettingsTable(conn, catalog);
                    ensureOrderTable(conn, catalog);
                    ensureOrderItemTable(conn, catalog);
                    ensureHangOrderTable(conn, catalog);
                    ensurePointRewardTable(conn, catalog);
                    // 已有：供应商与入库相关
                    ensureSupplierTable(conn, catalog);
                    ensureStockInTable(conn, catalog);
                    ensureStockInItemTable(conn, catalog);
                } catch (Exception ex) {
                    System.err.println("[SchemaInit] 租户="+tenantId+" 初始化入库/供应商表失败: " + ex.getMessage());
                }
            }
            // 基准租户表结构复制（rzt 作为基准，如果存在）
            if (tenantDataSources.containsKey("rzt")) {
                System.out.println("[SchemaInit] 发现基准租户 rzt，开始同步其表结构到其它租户...");
                try (Connection baseConn = tenantDataSources.get("rzt").getConnection()) {
                    String baseCatalog = baseConn.getCatalog();
                    Set<String> baseTables = listTables(baseConn, baseCatalog);
                    // 需要跳过的表（如 flyway、临时表等）
                    Set<String> skip = new HashSet<>(Arrays.asList("flyway_schema_history"));
                    for (String tenantId : tenantDataSources.keySet()) {
                        if ("rzt".equals(tenantId)) continue; // 跳过自己
                        DataSource targetDs = tenantDataSources.get(tenantId);
                        try (Connection targetConn = targetDs.getConnection()) {
                            String targetCatalog = targetConn.getCatalog();
                            Set<String> targetTables = listTables(targetConn, targetCatalog);
                            for (String table : baseTables) {
                                if (skip.contains(table)) continue;
                                if (!targetTables.contains(table)) {
                                    // 复制结构
                                    String ddl = fetchCreateTableDDL(baseConn, baseCatalog, table);
                                    if (ddl != null) {
                                        // 替换可能包含的基准库名（防止跨库引用）
                                        ddl = ddl.replace("`"+baseCatalog+"`.", "");
                                        try (Statement st = targetConn.createStatement()) {
                                            st.executeUpdate(ddl);
                                            System.out.println("[SchemaInit] 已为租户="+tenantId+" 创建缺失表 " + table);
                                        } catch (SQLException ce) {
                                            System.err.println("[SchemaInit] 创建表 " + table + " 失败(租户="+tenantId+"): " + ce.getMessage());
                                        }
                                    }
                                }
                            }
                        } catch (Exception te) {
                            System.err.println("[SchemaInit] 同步到租户="+tenantId+" 失败: "+te.getMessage());
                        }
                    }
                } catch (Exception be) {
                    System.err.println("[SchemaInit] 基准租户 rzt 同步过程失败: "+be.getMessage());
                }
            } else {
                System.out.println("[SchemaInit] 未找到基准租户 rzt，跳过跨租户表结构同步");
            }
            System.out.println("[SchemaInit] 入库 & 供应商相关表检测及同步完成: " + LocalDateTime.now());
        };
    }

    private Set<String> listTables(Connection conn, String catalog) throws SQLException {
        Set<String> set = new HashSet<>();
        try (ResultSet rs = conn.getMetaData().getTables(catalog, null, "%", new String[]{"TABLE"})) {
            while (rs.next()) {
                set.add(rs.getString("TABLE_NAME"));
            }
        }
        return set;
    }

    private String fetchCreateTableDDL(Connection conn, String catalog, String table) {
        String sql = "SHOW CREATE TABLE `" + catalog + "`.`" + table + "`";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getString(2); // 第二列是DDL
            }
        } catch (SQLException e) {
            System.err.println("[SchemaInit] 获取DDL失败 table="+table+" msg="+e.getMessage());
        }
        return null;
    }

    private void ensureSupplierTable(Connection conn, String catalog) throws SQLException {
        if (!tableExists(conn, catalog, "supplier")) {
            String ddl = "CREATE TABLE supplier (" +
                    "supplier_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "supplier_name VARCHAR(100) NOT NULL, " +
                    "contact_person VARCHAR(50) NULL, " +
                    "phone VARCHAR(20) NULL, " +
                    "address VARCHAR(200) NULL, " +
                    "create_time DATETIME NULL, " +
                    "INDEX idx_supplier_name(supplier_name)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
            try (Statement st = conn.createStatement()) {
                st.executeUpdate(ddl);
                System.out.println("[SchemaInit] 已创建表 supplier");
            }
        } else {
            addColumnIfMissing(conn, "supplier", "supplier_name", "VARCHAR(100) NOT NULL");
            addColumnIfMissing(conn, "supplier", "contact_person", "VARCHAR(50) NULL");
            addColumnIfMissing(conn, "supplier", "phone", "VARCHAR(20) NULL");
            addColumnIfMissing(conn, "supplier", "address", "VARCHAR(200) NULL");
            addColumnIfMissing(conn, "supplier", "create_time", "DATETIME NULL");
        }
    }

    private void ensureStockInTable(Connection conn, String catalog) throws SQLException {
        if (!tableExists(conn, catalog, "stock_in")) {
            String ddl = "CREATE TABLE stock_in (" +
                    "stock_in_id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "stock_in_no VARCHAR(32) NOT NULL UNIQUE, " +
                    "supplier_id INT NULL, " +
                    "stock_in_date DATETIME NOT NULL, " +
                    "total_amount DOUBLE NULL, " +
                    "operator_id INT NULL, " +
                    "status INT NULL, " +
                    "remark VARCHAR(500) NULL, " +
                    "create_time DATETIME NULL, " +
                    "update_time DATETIME NULL, " +
                    "INDEX idx_stock_in_date(stock_in_date), " +
                    "INDEX idx_stock_in_supplier(supplier_id)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
            try (Statement st = conn.createStatement()) {
                st.executeUpdate(ddl);
                System.out.println("[SchemaInit] 已创建表 stock_in");
            }
        } else {
            // 补充缺失列（避免之前手动建表字段不足）
            addColumnIfMissing(conn, "stock_in", "stock_in_no", "VARCHAR(32) NOT NULL UNIQUE");
            addColumnIfMissing(conn, "stock_in", "supplier_id", "INT NULL");
            addColumnIfMissing(conn, "stock_in", "stock_in_date", "DATETIME NOT NULL");
            addColumnIfMissing(conn, "stock_in", "total_amount", "DOUBLE NULL");
            addColumnIfMissing(conn, "stock_in", "operator_id", "INT NULL");
            addColumnIfMissing(conn, "stock_in", "status", "INT NULL");
            addColumnIfMissing(conn, "stock_in", "remark", "VARCHAR(500) NULL");
            addColumnIfMissing(conn, "stock_in", "create_time", "DATETIME NULL");
            addColumnIfMissing(conn, "stock_in", "update_time", "DATETIME NULL");
        }
    }

    private void ensureStockInItemTable(Connection conn, String catalog) throws SQLException {
        boolean medicineExists = tableExists(conn, catalog, "medicine");
        if (!tableExists(conn, catalog, "stock_in_item")) {
            String ddl = "CREATE TABLE stock_in_item (" +
                    "item_id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "stock_in_id BIGINT NOT NULL, " +
                    "medicine_id VARCHAR(32) NOT NULL, " +
                    "quantity INT NOT NULL, " +
                    "unit_price DOUBLE NOT NULL, " +
                    "batch_number VARCHAR(50) NULL, " +
                    "production_date DATE NULL, " +
                    "expiry_date DATE NULL, " +
                    "INDEX idx_stock_in_item_fk(stock_in_id), " +
                    "INDEX idx_stock_in_item_med(medicine_id)" +
                    (medicineExists ? ", CONSTRAINT fk_stock_item_in FOREIGN KEY (stock_in_id) REFERENCES stock_in(stock_in_id) ON DELETE CASCADE, CONSTRAINT fk_stock_item_med FOREIGN KEY (medicine_id) REFERENCES medicine(medicine_id) ON DELETE RESTRICT" : "") +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
            try (Statement st = conn.createStatement()) {
                st.executeUpdate(ddl);
                System.out.println("[SchemaInit] 已创建表 stock_in_item (medicineExists="+medicineExists+")");
            }
        } else {
            addColumnIfMissing(conn, "stock_in_item", "batch_number", "VARCHAR(50) NULL");
            addColumnIfMissing(conn, "stock_in_item", "production_date", "DATE NULL");
            addColumnIfMissing(conn, "stock_in_item", "expiry_date", "DATE NULL");
            // 若原先缺失外键且 medicine 表现在存在，可考虑后续人工补充，不在此强制 ALTER 以避免锁等待
        }
    }

    private void ensureMemberTable(Connection conn, String catalog) throws SQLException {
        if (!tableExists(conn, catalog, "member")) {
            String ddl = "CREATE TABLE member (" +
                    "member_id VARCHAR(32) PRIMARY KEY, " +
                    "name VARCHAR(50) NOT NULL, " +
                    "phone VARCHAR(20) NOT NULL, " +
                    "card_no VARCHAR(50), " +
                    "level TINYINT DEFAULT 0, " +
                    "points INT DEFAULT 0, " +
                    "allergic_history TEXT, " +
                    "medical_card_no VARCHAR(50), " +
                    "create_time DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "UNIQUE KEY uk_phone(phone)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
            try (Statement st = conn.createStatement()) { st.executeUpdate(ddl); System.out.println("[SchemaInit] 已创建表 member"); }
        } else {
            // 补齐缺失列（如果旧脚本仅建了基础列）
            addColumnIfMissing(conn, "member", "card_no", "VARCHAR(50)");
            addColumnIfMissing(conn, "member", "allergic_history", "TEXT");
            addColumnIfMissing(conn, "member", "medical_card_no", "VARCHAR(50)");
        }
    }
    private void ensureCategoryTable(Connection conn, String catalog) throws SQLException {
        if (!tableExists(conn, catalog, "category")) {
            String ddl = "CREATE TABLE category (" +
                    "category_id INT PRIMARY KEY AUTO_INCREMENT, " +
                    "category_name VARCHAR(50) NOT NULL, " +
                    "parent_id INT DEFAULT 0, " +
                    "sort INT DEFAULT 0, " +
                    "create_time DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "UNIQUE KEY uk_name(category_name)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
            try (Statement st = conn.createStatement()) { st.executeUpdate(ddl); System.out.println("[SchemaInit] 已创建表 category"); }
        }
    }
    private void ensureRoleTable(Connection conn, String catalog) throws SQLException {
        if (!tableExists(conn, catalog, "role")) {
            String ddl = "CREATE TABLE role (" +
                    "role_id INT PRIMARY KEY AUTO_INCREMENT, " +
                    "role_name VARCHAR(50) NOT NULL, " +
                    "permissions TEXT, " +
                    "UNIQUE KEY uk_role_name(role_name)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
            try (Statement st = conn.createStatement()) { st.executeUpdate(ddl); System.out.println("[SchemaInit] 已创建表 role"); }
        }
    }
    private void ensureEmployeeTable(Connection conn, String catalog) throws SQLException {
        if (!tableExists(conn, catalog, "employee")) {
            String ddl = "CREATE TABLE employee (" +
                    "employee_id INT PRIMARY KEY AUTO_INCREMENT, " +
                    "username VARCHAR(50) NOT NULL, " +
                    "password VARCHAR(100) NOT NULL, " +
                    "name VARCHAR(50) NOT NULL, " +
                    "role_id INT NOT NULL, " +
                    "phone VARCHAR(20), " +
                    "status TINYINT DEFAULT 1, " +
                    "create_time DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "UNIQUE KEY uk_username(username), " +
                    "KEY idx_role(role_id)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
            try (Statement st = conn.createStatement()) { st.executeUpdate(ddl); System.out.println("[SchemaInit] 已创建表 employee"); }
        }
    }
    private void ensureMedicineTable(Connection conn, String catalog) throws SQLException {
        if (!tableExists(conn, catalog, "medicine")) {
            String ddl = "CREATE TABLE medicine (" +
                    "medicine_id VARCHAR(32) PRIMARY KEY, " +
                    "generic_name VARCHAR(100) NOT NULL, " +
                    "trade_name VARCHAR(100), " +
                    "spec VARCHAR(50) NOT NULL, " +
                    "approval_no VARCHAR(50) NOT NULL, " +
                    "category_id INT NOT NULL, " +
                    "manufacturer VARCHAR(100), " +
                    "barcode VARCHAR(64), " +
                    "retail_price DECIMAL(10,2) NOT NULL, " +
                    "member_price DECIMAL(10,2), " +
                    "production_date DATE, " +
                    "expiry_date DATE, " +
                    "status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE', " +
                    "deleted TINYINT(1) NOT NULL DEFAULT 0, " +
                    "is_rx TINYINT(1) NOT NULL DEFAULT 0, " +
                    "unit VARCHAR(20), " +
                    "description TEXT, " +
                    "supplier_id INT, " +
                    "dosage_form VARCHAR(50), " +
                    "product_code VARCHAR(50), " +
                    "usage_dosage VARCHAR(255), " +
                    "contraindication VARCHAR(255), " +
                    "create_time DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                    "KEY idx_category(category_id)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
            try (Statement st = conn.createStatement()) { st.executeUpdate(ddl); System.out.println("[SchemaInit] 已创建表 medicine"); }
        } else {
            addColumnIfMissing(conn, "medicine", "barcode", "VARCHAR(64)");
            addColumnIfMissing(conn, "medicine", "production_date", "DATE");
            addColumnIfMissing(conn, "medicine", "expiry_date", "DATE");
            addColumnIfMissing(conn, "medicine", "status", "VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'");
            addColumnIfMissing(conn, "medicine", "deleted", "TINYINT(1) NOT NULL DEFAULT 0");
            addColumnIfMissing(conn, "medicine", "is_rx", "TINYINT(1) NOT NULL DEFAULT 0");
            addColumnIfMissing(conn, "medicine", "unit", "VARCHAR(20)");
            addColumnIfMissing(conn, "medicine", "description", "TEXT");
            addColumnIfMissing(conn, "medicine", "supplier_id", "INT");
            addColumnIfMissing(conn, "medicine", "usage_dosage", "VARCHAR(255)");
            addColumnIfMissing(conn, "medicine", "contraindication", "VARCHAR(255)");
            addColumnIfMissing(conn, "medicine", "dosage_form", "VARCHAR(50)");
            addColumnIfMissing(conn, "medicine", "product_code", "VARCHAR(50)");
            addColumnIfMissing(conn, "medicine", "manufacturer", "VARCHAR(100)");
        }
    }
    private void ensureInventoryTable(Connection conn, String catalog) throws SQLException {
        if (!tableExists(conn, catalog, "inventory")) {
            String ddl = "CREATE TABLE inventory (" +
                    "inventory_id INT PRIMARY KEY AUTO_INCREMENT, " +
                    "medicine_id VARCHAR(32) NOT NULL, " +
                    "batch_no VARCHAR(50), " +
                    "create_time DATETIME, " +
                    "expiry_date DATE, " +
                    "stock_quantity INT NOT NULL DEFAULT 0, " +
                    "min_stock INT DEFAULT 10, " +
                    "max_stock INT, " +
                    "purchase_price DECIMAL(10,2), " +
                    "supplier VARCHAR(100), " +
                    "update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                    "warehouse VARCHAR(50), " +
                    "inbound_date DATETIME, " +
                    "acceptance_no VARCHAR(50), " +
                    "invoice_no VARCHAR(50), " +
                    "invoice_date DATETIME, " +
                    "acceptance_date DATETIME, " +
                    "status VARCHAR(20), " +
                    "KEY idx_med(medicine_id)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
            try (Statement st = conn.createStatement()) { st.executeUpdate(ddl); System.out.println("[SchemaInit] 已创建表 inventory"); }
        } else {
            addColumnIfMissing(conn, "inventory", "max_stock", "INT");
            addColumnIfMissing(conn, "inventory", "min_stock", "INT DEFAULT 10");
            addColumnIfMissing(conn, "inventory", "purchase_price", "DECIMAL(10,2)");
            addColumnIfMissing(conn, "inventory", "supplier", "VARCHAR(100)");
            // 新增：入库与库存明细字段
            addColumnIfMissing(conn, "inventory", "warehouse", "VARCHAR(50)");
            addColumnIfMissing(conn, "inventory", "inbound_date", "DATETIME");
            addColumnIfMissing(conn, "inventory", "acceptance_no", "VARCHAR(50)");
            addColumnIfMissing(conn, "inventory", "invoice_no", "VARCHAR(50)");
            addColumnIfMissing(conn, "inventory", "invoice_date", "DATETIME");
            addColumnIfMissing(conn, "inventory", "acceptance_date", "DATETIME");
            addColumnIfMissing(conn, "inventory", "status", "VARCHAR(20)");
        }
    }
    private void ensureOrderTable(Connection conn, String catalog) throws SQLException {
        if (!tableExists(conn, catalog, "order")) {
            String ddl = "CREATE TABLE `order` (" +
                    "order_id VARCHAR(32) PRIMARY KEY, " +
                    "customer_name VARCHAR(100), " +
                    "member_id VARCHAR(32), " +
                    "cashier_id INT, " +
                    "total_amount DECIMAL(10,2) NOT NULL DEFAULT 0, " +
                    "discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0, " +
                    "actual_payment DECIMAL(10,2) NOT NULL DEFAULT 0, " +
                    "payment_type TINYINT DEFAULT 1, " +
                    "payment_status TINYINT DEFAULT 1, " +
                    "order_time DATETIME, " +
                    "pay_time DATETIME, " +
                    "refund_time DATETIME, " +
                    "refund_reason VARCHAR(200), " +
                    "used_points INT, " +
                    "created_points INT, " +
                    "remark VARCHAR(200), " +
                    "KEY idx_member(member_id), " +
                    "KEY idx_cashier(cashier_id)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
            try (Statement st = conn.createStatement()) { st.executeUpdate(ddl); System.out.println("[SchemaInit] 已创建表 order"); }
        }
    }
    private void ensureOrderItemTable(Connection conn, String catalog) throws SQLException {
        if (!tableExists(conn, catalog, "order_item")) {
            String ddl = "CREATE TABLE order_item (" +
                    "id INT PRIMARY KEY AUTO_INCREMENT, " +
                    "order_id VARCHAR(32) NOT NULL, " +
                    "medicine_id VARCHAR(32) NOT NULL, " +
                    "prescription_id VARCHAR(32) NULL, " +
                    "quantity INT NOT NULL, " +
                    "unit_price DECIMAL(10,2) NOT NULL, " +
                    "subtotal DECIMAL(10,2) NOT NULL, " +
                    "KEY idx_order(order_id), " +
                    "KEY idx_med(medicine_id)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
            try (Statement st = conn.createStatement()) { st.executeUpdate(ddl); System.out.println("[SchemaInit] 已创建表 order_item"); }
        } else {
            addColumnIfMissing(conn, "order_item", "prescription_id", "VARCHAR(32) NULL");
        }
    }

    private void ensureSystemSettingsTable(Connection conn, String catalog) throws SQLException {
        if (!tableExists(conn, catalog, "system_settings")) {
            String ddl = "CREATE TABLE system_settings (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "store_name VARCHAR(255) NULL, " +
                    "store_phone VARCHAR(50) NULL, " +
                    "store_address VARCHAR(255) NULL, " +
                    "store_desc VARCHAR(255) NULL, " +
                    "low_stock_threshold INT NULL, " +
                    "notify_methods VARCHAR(255) NULL, " +
                    "points_rule DOUBLE NULL, " +
                    "cash_rule INT NULL, " +
                    "operation_log TINYINT(1) NULL, " +
                    "created_at DATETIME NULL, " +
                    "updated_at DATETIME NULL, " +
                    "open_time VARCHAR(10) NULL, " +
                    "close_time VARCHAR(10) NULL, " +
                    "license_number VARCHAR(64) NULL, " +
                    "notify_system TINYINT(1) NULL, " +
                    "notify_email TINYINT(1) NULL, " +
                    "notify_sms TINYINT(1) NULL, " +
                    "notify_email_address VARCHAR(128) NULL, " +
                    "email_frequency VARCHAR(20) NULL, " +
                    "notify_phone VARCHAR(20) NULL, " +
                    "sms_frequency VARCHAR(20) NULL, " +
                    "inventory_check_cycle VARCHAR(20) NULL, " +
                    "normal_to_silver INT NULL, " +
                    "normal_discount INT NULL, " +
                    "silver_to_gold INT NULL, " +
                    "silver_discount INT NULL, " +
                    "gold_to_platinum INT NULL, " +
                    "gold_discount INT NULL, " +
                    "enable_cash TINYINT(1) NULL, " +
                    "enable_wechat TINYINT(1) NULL, " +
                    "enable_alipay TINYINT(1) NULL, " +
                    "enable_member_card TINYINT(1) NULL, " +
                    "wechat_mch_id VARCHAR(64) NULL, " +
                    "wechat_api_key VARCHAR(128) NULL, " +
                    "alipay_app_id VARCHAR(64) NULL, " +
                    "alipay_private_key TEXT NULL, " +
                    "change_unit VARCHAR(10) NULL, " +
                    "default_printer VARCHAR(32) NULL, " +
                    "paper_size VARCHAR(32) NULL, " +
                    "print_logo TINYINT(1) NULL, " +
                    "print_qrcode TINYINT(1) NULL, " +
                    "print_member TINYINT(1) NULL, " +
                    "print_drug_detail TINYINT(1) NULL, " +
                    "print_usage TINYINT(1) NULL, " +
                    "print_footer TINYINT(1) NULL, " +
                    "footer_text VARCHAR(512) NULL, " +
                    "receipt_copies INT NULL, " +
                    "prescription_copies INT NULL, " +
                    "enable_log TINYINT(1) NULL, " +
                    "log_retention INT NULL, " +
                    "pwd_expiry INT NULL, " +
                    "login_attempts INT NULL, " +
                    "enable_ip_restrict TINYINT(1) NULL, " +
                    "auto_backup TINYINT(1) NULL, " +
                    "backup_cycle VARCHAR(20) NULL, " +
                    "backup_retention INT NULL" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
            try (Statement st = conn.createStatement()) {
                st.executeUpdate(ddl);
                System.out.println("[SchemaInit] 已创建表 system_settings");
            }
        } else {
            addColumnIfMissing(conn, "system_settings", "store_name", "VARCHAR(255) NULL");
            addColumnIfMissing(conn, "system_settings", "store_phone", "VARCHAR(50) NULL");
            addColumnIfMissing(conn, "system_settings", "store_address", "VARCHAR(255) NULL");
            addColumnIfMissing(conn, "system_settings", "store_desc", "VARCHAR(255) NULL");
            addColumnIfMissing(conn, "system_settings", "low_stock_threshold", "INT NULL");
            addColumnIfMissing(conn, "system_settings", "notify_methods", "VARCHAR(255) NULL");
            addColumnIfMissing(conn, "system_settings", "points_rule", "DOUBLE NULL");
            addColumnIfMissing(conn, "system_settings", "cash_rule", "INT NULL");
            addColumnIfMissing(conn, "system_settings", "operation_log", "TINYINT(1) NULL");
            addColumnIfMissing(conn, "system_settings", "created_at", "DATETIME NULL");
            addColumnIfMissing(conn, "system_settings", "updated_at", "DATETIME NULL");
            addColumnIfMissing(conn, "system_settings", "open_time", "VARCHAR(10) NULL");
            addColumnIfMissing(conn, "system_settings", "close_time", "VARCHAR(10) NULL");
            addColumnIfMissing(conn, "system_settings", "license_number", "VARCHAR(64) NULL");
            addColumnIfMissing(conn, "system_settings", "notify_system", "TINYINT(1) NULL");
            addColumnIfMissing(conn, "system_settings", "notify_email", "TINYINT(1) NULL");
            addColumnIfMissing(conn, "system_settings", "notify_sms", "TINYINT(1) NULL");
            addColumnIfMissing(conn, "system_settings", "notify_email_address", "VARCHAR(128) NULL");
            addColumnIfMissing(conn, "system_settings", "email_frequency", "VARCHAR(20) NULL");
            addColumnIfMissing(conn, "system_settings", "notify_phone", "VARCHAR(20) NULL");
            addColumnIfMissing(conn, "system_settings", "sms_frequency", "VARCHAR(20) NULL");
            addColumnIfMissing(conn, "system_settings", "inventory_check_cycle", "VARCHAR(20) NULL");
            addColumnIfMissing(conn, "system_settings", "normal_to_silver", "INT NULL");
            addColumnIfMissing(conn, "system_settings", "normal_discount", "INT NULL");
            addColumnIfMissing(conn, "system_settings", "silver_to_gold", "INT NULL");
            addColumnIfMissing(conn, "system_settings", "silver_discount", "INT NULL");
            addColumnIfMissing(conn, "system_settings", "gold_to_platinum", "INT NULL");
            addColumnIfMissing(conn, "system_settings", "gold_discount", "INT NULL");
            addColumnIfMissing(conn, "system_settings", "enable_cash", "TINYINT(1) NULL");
            addColumnIfMissing(conn, "system_settings", "enable_wechat", "TINYINT(1) NULL");
            addColumnIfMissing(conn, "system_settings", "enable_alipay", "TINYINT(1) NULL");
            addColumnIfMissing(conn, "system_settings", "enable_member_card", "TINYINT(1) NULL");
            addColumnIfMissing(conn, "system_settings", "wechat_mch_id", "VARCHAR(64) NULL");
            addColumnIfMissing(conn, "system_settings", "wechat_api_key", "VARCHAR(128) NULL");
            addColumnIfMissing(conn, "system_settings", "alipay_app_id", "VARCHAR(64) NULL");
            addColumnIfMissing(conn, "system_settings", "alipay_private_key", "TEXT NULL");
            addColumnIfMissing(conn, "system_settings", "change_unit", "VARCHAR(10) NULL");
            addColumnIfMissing(conn, "system_settings", "default_printer", "VARCHAR(32) NULL");
            addColumnIfMissing(conn, "system_settings", "paper_size", "VARCHAR(32) NULL");
            addColumnIfMissing(conn, "system_settings", "print_logo", "TINYINT(1) NULL");
            addColumnIfMissing(conn, "system_settings", "print_qrcode", "TINYINT(1) NULL");
            addColumnIfMissing(conn, "system_settings", "print_member", "TINYINT(1) NULL");
            addColumnIfMissing(conn, "system_settings", "print_drug_detail", "TINYINT(1) NULL");
            addColumnIfMissing(conn, "system_settings", "print_usage", "TINYINT(1) NULL");
            addColumnIfMissing(conn, "system_settings", "print_footer", "TINYINT(1) NULL");
            addColumnIfMissing(conn, "system_settings", "footer_text", "VARCHAR(512) NULL");
            addColumnIfMissing(conn, "system_settings", "receipt_copies", "INT NULL");
            addColumnIfMissing(conn, "system_settings", "prescription_copies", "INT NULL");
            addColumnIfMissing(conn, "system_settings", "enable_log", "TINYINT(1) NULL");
            addColumnIfMissing(conn, "system_settings", "log_retention", "INT NULL");
            addColumnIfMissing(conn, "system_settings", "pwd_expiry", "INT NULL");
            addColumnIfMissing(conn, "system_settings", "login_attempts", "INT NULL");
            addColumnIfMissing(conn, "system_settings", "enable_ip_restrict", "TINYINT(1) NULL");
            addColumnIfMissing(conn, "system_settings", "auto_backup", "TINYINT(1) NULL");
            addColumnIfMissing(conn, "system_settings", "backup_cycle", "VARCHAR(20) NULL");
            addColumnIfMissing(conn, "system_settings", "backup_retention", "INT NULL");
        }
    }

    private void ensureHangOrderTable(Connection conn, String catalog) throws SQLException {
        if (!tableExists(conn, catalog, "hang_order")) {
            String ddl = "CREATE TABLE hang_order (" +
                    "hang_id VARCHAR(32) PRIMARY KEY, " +
                    "hang_time DATETIME, " +
                    "cart_json TEXT, " +
                    "member_id VARCHAR(32), " +
                    "member_name VARCHAR(50), " +
                    "cashier_id INT, " +
                    "remark VARCHAR(255), " +
                    "status INT DEFAULT 1, " +
                    "create_time DATETIME DEFAULT CURRENT_TIMESTAMP" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
            try (Statement st = conn.createStatement()) { st.executeUpdate(ddl); System.out.println("[SchemaInit] 已创建表 hang_order"); }
        } else {
            addColumnIfMissing(conn, "hang_order", "cart_json", "TEXT");
            addColumnIfMissing(conn, "hang_order", "member_id", "VARCHAR(32)");
            addColumnIfMissing(conn, "hang_order", "member_name", "VARCHAR(50)");
            addColumnIfMissing(conn, "hang_order", "cashier_id", "INT");
            addColumnIfMissing(conn, "hang_order", "remark", "VARCHAR(255)");
            addColumnIfMissing(conn, "hang_order", "status", "INT DEFAULT 1");
        }
    }

    private void ensurePointRewardTable(Connection conn, String catalog) throws SQLException {
        if (!tableExists(conn, catalog, "point_reward")) {
            String ddl = "CREATE TABLE point_reward (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "name VARCHAR(255) NOT NULL, " +
                    "points_required INT NOT NULL, " +
                    "description VARCHAR(500), " +
                    "is_active BOOLEAN NOT NULL DEFAULT TRUE, " +
                    "create_time DATETIME(6), " +
                    "update_time DATETIME(6)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
            try (Statement st = conn.createStatement()) { st.executeUpdate(ddl); System.out.println("[SchemaInit] 已创建表 point_reward"); }
        }
    }

    private void ensureProductTransferLogTable(Connection conn, String catalog) throws SQLException {
        if (!tableExists(conn, catalog, "product_transfer_log")) {
            String ddl = "CREATE TABLE product_transfer_log (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "from_tenant VARCHAR(50) NOT NULL, " +
                    "to_tenant VARCHAR(50) NOT NULL, " +
                    "medicine_id VARCHAR(50) NOT NULL, " +
                    "batch_no VARCHAR(50), " +
                    "quantity INT NOT NULL, " +
                    "operator_id VARCHAR(50), " +
                    "remark VARCHAR(255), " +
                    "transfer_time DATETIME NOT NULL, " +
                    "trace_id VARCHAR(50)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
            try (Statement st = conn.createStatement()) {
                st.executeUpdate(ddl);
                System.out.println("[SchemaInit] Created table product_transfer_log in " + catalog);
            }
        }
    }

    private void ensureBossTable(Connection conn, String catalog) throws SQLException {
        if (!tableExists(conn, catalog, "boss")) {
            String ddl = "CREATE TABLE boss (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "username VARCHAR(255) NOT NULL UNIQUE, " +
                    "password VARCHAR(255) NOT NULL" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci";
            try (Statement st = conn.createStatement()) {
                st.executeUpdate(ddl);
                System.out.println("[SchemaInit] Created table boss in " + catalog);
            }
        }
    }

    private boolean tableExists(Connection conn, String catalog, String table) {
        try (ResultSet rs = conn.getMetaData().getTables(catalog, null, table, null)) {
            return rs.next();
        } catch (SQLException e) {
            System.err.println("[SchemaInit] tableExists 查询失败 catalog="+catalog+" table="+table+" msg="+e.getMessage());
            return false;
        }
    }

    private void addColumnIfMissing(Connection conn, String table, String column, String definition) {
        try {
            String sqlCheck = "SELECT COUNT(*) FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='" + escape(conn.getCatalog()) + "' AND TABLE_NAME='" + table + "' AND COLUMN_NAME='" + column + "'";
            try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sqlCheck)) {
                if (rs.next() && rs.getInt(1) == 0) {
                    String alter = "ALTER TABLE " + table + " ADD COLUMN " + column + " " + definition;
                    try (Statement stAlter = conn.createStatement()) {
                        stAlter.executeUpdate(alter);
                        System.out.println("[SchemaInit] 表 " + table + " 已补充列 " + column);
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("[SchemaInit] 检测/添加列失败 table="+table+" column="+column+" msg="+e.getMessage());
        }
    }

    private String escape(String s){
        return s == null ? "" : s.replace("'", "''");
    }
}
