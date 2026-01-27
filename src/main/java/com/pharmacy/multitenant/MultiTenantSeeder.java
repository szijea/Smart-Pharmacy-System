package com.pharmacy.multitenant;

import com.pharmacy.multitenant.TenantContext;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.*;

/**
 * 在应用启动后对所有租户进行最小化的表与初始账号/角色校验与补种。
 * 解决多数据库尚未手工导入完整 schema 时无法登录的问题。
 * 如果目标库已存在这些表与数据，将保持不变（幂等）。
 */
@Component
public class MultiTenantSeeder implements ApplicationRunner {

    private final MultiTenantDataSourceConfig multiTenantDataSourceConfig;
    private final DataSource defaultDataSource;

    public MultiTenantSeeder(MultiTenantDataSourceConfig multiTenantDataSourceConfig,
                             @org.springframework.beans.factory.annotation.Qualifier("defaultDataSource") DataSource defaultDataSource) {
        this.multiTenantDataSourceConfig = multiTenantDataSourceConfig;
        this.defaultDataSource = defaultDataSource;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) throws Exception {
        Map<String, DataSource> tenants = multiTenantDataSourceConfig.getDataSourceMap();
        Map<String, DataSource> allDataSources = new HashMap<>(tenants);
        allDataSources.put("default", defaultDataSource);

        System.out.println("[Seeder] Begin provisioning tenants: " + allDataSources.keySet());
        for(String tenant : allDataSources.keySet()){
            DataSource ds = allDataSources.get(tenant);
            if(ds == null) {
                System.err.println("[Seeder] Skip tenant="+tenant+" (null DataSource)");
                continue;
            }
            TenantContext.setTenant(tenant);
            try {
                provisionTenant(tenant, ds);
            } catch (Exception ex){
                System.err.println("[Seeder] Provision failed tenant="+tenant+" error="+ex.getMessage());
            } finally {
                TenantContext.clear();
            }
        }
        System.out.println("[Seeder] Provision finished.");
    }

    private void provisionTenant(String tenant, DataSource ds){
        JdbcTemplate jdbc = new JdbcTemplate(ds);
        // 确认 role 表是否存在
        if(!tableExists(jdbc, "role")){
            jdbc.execute("CREATE TABLE IF NOT EXISTS role (" +
                    "role_id INT PRIMARY KEY AUTO_INCREMENT," +
                    "role_name VARCHAR(50) NOT NULL UNIQUE," +
                    "permissions TEXT) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            System.out.println("[Seeder] Created table role for tenant="+tenant);
        }
        // 确认 employee 表是否存在
        if(!tableExists(jdbc, "employee")){
            jdbc.execute("CREATE TABLE IF NOT EXISTS employee (" +
                    "employee_id INT PRIMARY KEY AUTO_INCREMENT," +
                    "username VARCHAR(50) NOT NULL UNIQUE," +
                    "password VARCHAR(100) NOT NULL," +
                    "name VARCHAR(50) NOT NULL," +
                    "role_id INT NOT NULL," +
                    "phone VARCHAR(20)," +
                    "status TINYINT DEFAULT 1," +
                    "create_time DATETIME DEFAULT CURRENT_TIMESTAMP," +
                    "KEY idx_role (role_id)," +
                    "CONSTRAINT fk_employee_role FOREIGN KEY (role_id) REFERENCES role(role_id)" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            System.out.println("[Seeder] Created table employee for tenant="+tenant);
        }
        // 创建 supplier 表
        if(!tableExists(jdbc, "supplier")){
            jdbc.execute("CREATE TABLE IF NOT EXISTS supplier (" +
                    "supplier_id INT PRIMARY KEY AUTO_INCREMENT," +
                    "supplier_name VARCHAR(100) NOT NULL," +
                    "contact_person VARCHAR(50)," +
                    "phone VARCHAR(20)," +
                    "address VARCHAR(200)," +
                    "create_time DATETIME DEFAULT CURRENT_TIMESTAMP" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            System.out.println("[Seeder] Created table supplier for tenant="+tenant);
        }
        // 默认供应商插入（ID=1 不保证，按名称判断）
        Integer supCount = jdbc.queryForObject("SELECT COUNT(*) FROM supplier WHERE supplier_name=?", Integer.class, "默认供应商");
        if(supCount != null && supCount == 0){
            jdbc.update("INSERT INTO supplier(supplier_name,contact_person,phone,address) VALUES(?,?,?,?)",
                    "默认供应商","系统","13800000000","系统自动创建");
            System.out.println("[Seeder] Insert default supplier tenant="+tenant);
        }

        // 插入角色（幂等）
        upsertRole(jdbc, "管理员", "[\"cashier\",\"inventory\",\"member\",\"order\",\"analysis\",\"system\"]");
        upsertRole(jdbc, "收银员", "[\"cashier\",\"order\",\"member\"]");
        upsertRole(jdbc, "库管员", "[\"inventory\",\"stock_record\"]");

        // 查询角色ID
        Integer adminRoleId   = queryRoleId(jdbc, "管理员");
        Integer cashierRoleId = queryRoleId(jdbc, "收银员");
        Integer stockRoleId   = queryRoleId(jdbc, "库管员");

        Map<String,List<String>> tenantUsers = new HashMap<>();
        tenantUsers.put("bht", Arrays.asList("adminbht:管理员", "bht01:收银员", "bht02:库管员"));
        tenantUsers.put("wx",  Arrays.asList("adminwx:管理员", "wx01:收银员", "wx02:库管员"));
        tenantUsers.put("rzt", Arrays.asList("adminrzt:管理员", "rzt01:收银员", "rzt02:库管员"));
        tenantUsers.put("default", Arrays.asList("admindef:管理员"));

        List<String> users = tenantUsers.getOrDefault(tenant, Collections.emptyList());
        for(String spec : users){
            String[] parts = spec.split(":");
            String username = parts[0];
            String roleName = parts[1];
            if(userExists(jdbc, username)) continue;
            Integer roleId = switch (roleName) {
                case "管理员" -> adminRoleId;
                case "收银员" -> cashierRoleId;
                case "库管员" -> stockRoleId;
                default -> adminRoleId;
            };
            // 默认密码 123456 的 MD5 e10adc3949ba59abbe56e057f20f883e
            jdbc.update("INSERT INTO employee(username,password,name,role_id,phone,status) VALUES(?,?,?,?,?,1)",
                    username,
                    "e10adc3949ba59abbe56e057f20f883e",
                    username,
                    roleId,
                    randomPhone());
            System.out.println("[Seeder] Insert user="+username+" tenant="+tenant);
        }
    }

    private boolean tableExists(JdbcTemplate jdbc, String table){
        try { jdbc.queryForObject("SELECT 1 FROM " + table + " LIMIT 1", Integer.class); return true; } catch (Exception e){ return false; }
    }

    private void upsertRole(JdbcTemplate jdbc, String roleName, String permissions){
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM role WHERE role_name=?", Integer.class, roleName);
        if(count != null && count > 0) return;
        jdbc.update("INSERT INTO role(role_name, permissions) VALUES(?,?)", roleName, permissions);
        System.out.println("[Seeder] Insert role="+roleName);
    }

    private Integer queryRoleId(JdbcTemplate jdbc, String roleName){
        try { return jdbc.queryForObject("SELECT role_id FROM role WHERE role_name=?", Integer.class, roleName); } catch (Exception e){ return null; }
    }

    private boolean userExists(JdbcTemplate jdbc, String username){
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM employee WHERE username=?", Integer.class, username);
        return count != null && count > 0;
    }

    private String randomPhone(){
        return "13" + (int)(Math.random()*8+1) + String.valueOf(System.currentTimeMillis()).substring(7,11);
    }
}

