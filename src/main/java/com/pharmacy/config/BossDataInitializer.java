package com.pharmacy.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import com.pharmacy.multitenant.TenantContext;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import java.util.Objects;

@Component
public class BossDataInitializer implements CommandLineRunner {

    @Autowired
    @Qualifier("defaultDataSource")
    private DataSource dataSource;

    @Override
    public void run(String... args) throws Exception {
        try {
            TenantContext.setCurrentTenant("default");

            JdbcTemplate jdbc = new JdbcTemplate(Objects.requireNonNull(dataSource, "dataSource"));
            jdbc.execute("CREATE TABLE IF NOT EXISTS boss (" +
                    "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                    "username VARCHAR(50) NOT NULL UNIQUE, " +
                    "password VARCHAR(100) NOT NULL" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");

            // Check if the boss user already exists using JDBC to avoid JPA overhead/startup issues
            Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM boss WHERE username = ?", Integer.class, "boss");
            if (count != null && count == 0) {
                // For a real application, passwords should be hashed.
                // Storing plain text as per the initial request.
                jdbc.update("INSERT INTO boss (username, password) VALUES (?, ?)", "boss", "123456");
                System.out.println("Default boss user created with username 'boss' and password '123456'.");
            }
        } finally {
            TenantContext.clear();
        }
    }
}
