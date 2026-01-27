package com.pharmacy.controller;

import com.pharmacy.multitenant.TenantContext;
import com.pharmacy.multitenant.MultiTenantDataSourceConfig;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminTenantController {

    private final MultiTenantDataSourceConfig config;
    private final javax.sql.DataSource routingDataSource;
    private final com.pharmacy.repository.EmployeeRepository employeeRepository; // 当前租户视角注入
    public AdminTenantController(MultiTenantDataSourceConfig config, DataSource routingDataSource, com.pharmacy.repository.EmployeeRepository employeeRepository){
        this.config = config;
        this.routingDataSource = routingDataSource;
        this.employeeRepository = employeeRepository;
    }

    @GetMapping("/tenant-info")
    public Map<String,Object> tenantInfo(){
        Map<String,Object> resp = new HashMap<>();
        String current = TenantContext.getTenant();
        resp.put("currentTenant", current == null ? "default" : current);
        resp.put("tenants", config.getTenantIds());
        resp.put("routingKeys", config.getTenantIds());
        resp.put("message", "OK");
        return resp;
    }

    @GetMapping("/tenant-connection")
    public Map<String,Object> tenantConnection() {
        Map<String,Object> resp = new HashMap<>();
        String current = TenantContext.getTenant();
        resp.put("currentTenant", current == null ? "default" : current);
        try (Connection c = routingDataSource.getConnection()) {
            DatabaseMetaData meta = c.getMetaData();
            resp.put("jdbcUrl", meta.getURL());
            resp.put("userName", meta.getUserName());
        } catch (Exception e){
            resp.put("error", e.getMessage());
        }
        resp.put("message", "OK");
        return resp;
    }

    @GetMapping("/tenant-employees-count")
    public Map<String,Object> tenantEmployeesCount(){
        Map<String,Object> resp = new HashMap<>();
        // 当前租户下的员工数量（验证是否切换成功）
        resp.put("currentTenant", TenantContext.getTenant()==null?"default":TenantContext.getTenant());
        resp.put("currentTenantEmployeeCount", employeeRepository.count());
        resp.put("hint", "如多个租户返回的 currentTenantEmployeeCount 一样且数据完全相同，可能使用了同一物理库或种子脚本插入了相同数据。");
        return resp;
    }

    @GetMapping("/repair-stock-in-tables")
    public Map<String,Object> repairStockInTables(){
        Map<String,Object> result = new HashMap<>();
        Map<String,Object> tenantsResult = new HashMap<>();
        for(String tenantId : config.getTenantIds()){
            DataSource ds = config.getDataSourceMap().get(tenantId);
            Map<String,Object> one = new HashMap<>();
            if(ds==null){
                one.put("error", "DataSource missing for tenant="+tenantId);
                tenantsResult.put(tenantId, one);
                continue;
            }
            try (Connection conn = ds.getConnection()) {
                String catalog = conn.getCatalog();
                one.put("catalog", catalog);
                ensureTable(conn, catalog, "supplier", () -> "CREATE TABLE supplier (" +
                        "supplier_id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "supplier_name VARCHAR(100) NOT NULL, " +
                        "contact_person VARCHAR(50) NULL, " +
                        "phone VARCHAR(20) NULL, " +
                        "address VARCHAR(200) NULL, " +
                        "create_time DATETIME NULL, " +
                        "INDEX idx_supplier_name(supplier_name)" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
                ensureTable(conn, catalog, "stock_in", () -> "CREATE TABLE stock_in (" +
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
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
                ensureTable(conn, catalog, "stock_in_item", () -> "CREATE TABLE stock_in_item (" +
                        "item_id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                        "stock_in_id BIGINT NOT NULL, " +
                        "medicine_id VARCHAR(32) NOT NULL, " +
                        "quantity INT NOT NULL, " +
                        "unit_price DOUBLE NOT NULL, " +
                        "batch_number VARCHAR(50) NULL, " +
                        "production_date DATE NULL, " +
                        "expiry_date DATE NULL, " +
                        "INDEX idx_stock_in_item_fk(stock_in_id), " +
                        "INDEX idx_stock_in_item_med(medicine_id), " +
                        "CONSTRAINT fk_stock_item_in FOREIGN KEY (stock_in_id) REFERENCES stock_in(stock_in_id) ON DELETE CASCADE, " +
                        "CONSTRAINT fk_stock_item_med FOREIGN KEY (medicine_id) REFERENCES medicine(medicine_id) ON DELETE RESTRICT" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
                one.put("status", "OK");
            } catch (Exception e){
                one.put("error", e.getMessage());
            }
            tenantsResult.put(tenantId, one);
        }
        result.put("tenants", tenantsResult);
        result.put("message", "repair executed");
        return result;
    }

    private void ensureTable(Connection conn, String catalog, String tableName, java.util.function.Supplier<String> ddlSupplier) {
        try (ResultSet rs = conn.getMetaData().getTables(catalog, null, tableName, null)) {
            if (!rs.next()) {
                try (Statement st = conn.createStatement()) {
                    st.executeUpdate(ddlSupplier.get());
                }
            }
        } catch (SQLException e){
            System.err.println("[AdminRepair] 创建表失败 table="+tableName+" msg="+e.getMessage());
        }
    }
}
