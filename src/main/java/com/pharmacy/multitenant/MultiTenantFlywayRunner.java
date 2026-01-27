package com.pharmacy.multitenant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * 在应用启动后原本用于对所有租户库执行 Flyway 迁移。
 * 目前为避免 MySQL 8 Unsupported Database 问题，已临时禁用实际迁移，仅保留日志。
 */
@Component
@Order(1)
public class MultiTenantFlywayRunner implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(MultiTenantFlywayRunner.class);

    @Override
    public void run(ApplicationArguments args) {
        // 临时禁用多租户 Flyway 迁移，避免 MySQL 8 Unsupported Database 错误，先保证应用能正常启动
        log.warn("[Flyway] MultiTenantFlywayRunner 已暂时禁用迁移执行，仅记录日志，不实际运行 Flyway.migrate()");
    }
}
