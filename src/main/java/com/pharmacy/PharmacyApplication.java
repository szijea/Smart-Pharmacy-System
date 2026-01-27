package com.pharmacy;

import com.pharmacy.multitenant.MultiTenantDataSourceConfig;
import com.pharmacy.multitenant.MultiTenantForeignKeyInitializer;
import com.pharmacy.multitenant.MultiTenantSchemaInitializer;
import com.pharmacy.multitenant.MultiTenantSupplierSeeder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(
        exclude = {
                org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration.class
        }
)
@EntityScan({"com.pharmacy.entity", "com.pharmacy.shared.entity"})
@EnableScheduling
@Import({MultiTenantDataSourceConfig.class, MultiTenantSchemaInitializer.class, MultiTenantSupplierSeeder.class, MultiTenantForeignKeyInitializer.class})
public class  PharmacyApplication {
    // Docker 部署说明:
    // 1. 通过 docker-compose 启动 mysql 与应用；init-multitenant.sql 会创建多租户库结构与基础数据。
    // 2. 应用启动后 MultiTenantSchemaInitializer 会补全 stock_in / stock_in_item / supplier 等缺失表。
    // 3. 已排除 FlywayAutoConfiguration，避免与手动/脚本初始化冲突；如需启用，请移除 exclude 并设置 flyway.enabled=true。
    public static void main(String[] args) {
        SpringApplication.run(PharmacyApplication.class, args);
    }
}
