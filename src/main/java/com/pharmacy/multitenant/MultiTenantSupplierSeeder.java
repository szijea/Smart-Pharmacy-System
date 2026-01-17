package com.pharmacy.multitenant;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.util.Map;

@Configuration
public class MultiTenantSupplierSeeder {

    @Bean
    @org.springframework.core.annotation.Order(50)
    public ApplicationRunner supplierDefaultSeeder(@Qualifier("tenantDataSources") Map<String, DataSource> tenantDataSources) {
        return args -> {
            System.out.println("[SupplierSeeder] 开始检测多租户默认供应商...");
            for (Map.Entry<String, DataSource> entry : tenantDataSources.entrySet()) {
                String tenantId = entry.getKey();
                if ("default".equals(tenantId)) continue;

                try {
                    JdbcTemplate jdbc = new JdbcTemplate(entry.getValue());
                    Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM supplier WHERE supplier_name = ?", Integer.class, "默认供应商");

                    if (count != null && count == 0) {
                         jdbc.update("INSERT INTO supplier(supplier_name, contact_person, phone, address, create_time) VALUES (?, ?, ?, ?, NOW())",
                                "默认供应商", "系统", "13800000000", "系统自动创建");
                        System.out.println("[SupplierSeeder] 租户="+tenantId+" 已创建默认供应商");
                    } else {
                        System.out.println("[SupplierSeeder] 租户="+tenantId+" 默认供应商已存在");
                    }
                } catch (Exception ex) {
                    System.err.println("[SupplierSeeder] 租户="+tenantId+" 创建默认供应商失败: "+ex.getMessage());
                }
            }
            System.out.println("[SupplierSeeder] 默认供应商检测完成");
        };
    }
}
