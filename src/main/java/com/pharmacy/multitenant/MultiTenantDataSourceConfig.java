package com.pharmacy.multitenant;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.*;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = "com.pharmacy.repository",
    entityManagerFactoryRef = "tenantEntityManagerFactory",
    transactionManagerRef = "tenantTransactionManager"
)
public class MultiTenantDataSourceConfig {

    private final Environment env;
    private final Map<String, DataSource> dataSourceMap = new java.util.concurrent.ConcurrentHashMap<>();
    private StoreRoutingDataSource currentRoutingDataSource;
    private DataSource defaultDataSource;

    public MultiTenantDataSourceConfig(Environment env) {
        this.env = env;
    }

    @Bean(name = "tenantDataSources")
    public Map<String, DataSource> tenantDataSources(
            @Value("${spring.datasource.username}") String defaultUser,
            @Value("${spring.datasource.password}") String defaultPwd) {
        if (!dataSourceMap.isEmpty()) {
            return dataSourceMap;
        }
        List<Map<String, Object>> tenants = loadTenantsFromEnv();

        for (Map<String, Object> t : tenants) {
            String id = Objects.toString(t.get("id"), null);
            String url = Objects.toString(t.get("url"), null);
            if (id == null || url == null) {
                System.err.println("[MultiTenant] Skipping invalid tenant config: " + t);
                continue;
            }
            String user = Objects.toString(t.get("username"), defaultUser);
            String pwd = Objects.toString(t.get("password"), defaultPwd);
            DataSource tenantDs = buildHikari(url, user, pwd, id);
            dataSourceMap.put(id, tenantDs);
        }
        return dataSourceMap;
    }

    @Bean(name = "routingDataSource")
    public DataSource routingDataSource(
            @Qualifier("defaultDataSource") DataSource defaultDataSource,
            @Qualifier("tenantDataSources") Map<String, DataSource> tenantDataSources) {
        this.defaultDataSource = Objects.requireNonNull(defaultDataSource, "defaultDataSource");
        Objects.requireNonNull(tenantDataSources, "tenantDataSources");
        StoreRoutingDataSource routing = new StoreRoutingDataSource();
        this.currentRoutingDataSource = routing;
        Map<Object, Object> targetDataSources = new HashMap<>(tenantDataSources);
        targetDataSources.put("default", this.defaultDataSource);

        routing.setTargetDataSources(targetDataSources);
        routing.setDefaultTargetDataSource((Object) Objects.requireNonNull(this.defaultDataSource, "defaultDataSource"));
        routing.afterPropertiesSet();
        System.out.println("[MultiTenant] Routing DataSources initialized: " + targetDataSources.keySet());
        return routing;
    }

    @Bean(name = "tenantEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean tenantEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier("routingDataSource") DataSource routingDataSource) {
        return builder
                .dataSource(routingDataSource)
                .packages("com.pharmacy.entity", "com.pharmacy.shared.entity")
                .persistenceUnit("tenant")
                .build();
    }

    @Bean(name = "tenantTransactionManager")
    public PlatformTransactionManager tenantTransactionManager(
            @Qualifier("tenantEntityManagerFactory") LocalContainerEntityManagerFactoryBean entityManagerFactory) {
        return new JpaTransactionManager(Objects.requireNonNull(entityManagerFactory.getObject(), "entityManagerFactory"));
    }

    private List<Map<String, Object>> loadTenantsFromEnv() {
        List<Map<String, Object>> list = new ArrayList<>();
        int idx = 0;
        while (true) {
            String prefix = "spring.tenants[" + idx + "]";
            String id = env.getProperty(prefix + ".id");
            if (id == null) break;
            Map<String, Object> item = new HashMap<>();
            item.put("id", id);
            item.put("url", env.getProperty(prefix + ".url"));
            item.put("username", env.getProperty(prefix + ".username"));
            item.put("password", env.getProperty(prefix + ".password"));
            list.add(item);
            idx++;
        }
        return list;
    }

    private DataSource buildHikari(String url, String user, String pwd, String poolName) {
        HikariConfig cfg = new HikariConfig();
        cfg.setJdbcUrl(url);
        cfg.setUsername(user);
        cfg.setPassword(pwd);
        cfg.setMaximumPoolSize(5);
        cfg.setPoolName("DS-" + poolName);
        cfg.setConnectionTestQuery("SELECT 1");
        cfg.addDataSourceProperty("cachePrepStmts", "true");
        cfg.addDataSourceProperty("prepStmtCacheSize", "250");
        cfg.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        try {
            System.out.println("[MultiTenant] Creating connection pool for tenant '" + poolName + "' -> " + url);
            return new HikariDataSource(cfg);
        } catch (Exception ex) {
            System.err.println("[MultiTenant] Failed to create connection pool for tenant '" + poolName + "': " + ex.getMessage());
            throw new RuntimeException("Failed to initialize datasource for tenant: " + poolName, ex);
        }
    }

    public Map<String, DataSource> getDataSourceMap() {
        return dataSourceMap;
    }

    public void addTenant(String id, String url, String username, String password) {
        if (dataSourceMap.containsKey(id)) {
            return;
        }
        DataSource ds = buildHikari(url, username, password, id);
        dataSourceMap.put(id, ds);
        refreshRoutingDataSource();
    }

    private synchronized void refreshRoutingDataSource() {
        if (currentRoutingDataSource == null) return;
        Map<Object, Object> targets = new HashMap<>(dataSourceMap);
        if (defaultDataSource != null) {
            targets.put("default", defaultDataSource);
        }
        currentRoutingDataSource.setTargetDataSources(targets);
        currentRoutingDataSource.afterPropertiesSet();
    }

    public Set<String> getTenantIds(){
        return dataSourceMap.keySet();
    }
}
