package com.pharmacy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/api/health")
public class HealthController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private com.pharmacy.multitenant.MultiTenantDataSourceConfig multiTenantDataSourceConfig;

    private static final long DEFAULT_TTL_MS = 30_000L; // 30s
    private volatile Map<String,Object> cachedSchemaResult;
    private volatile long cachedSchemaAt = 0L;
    private volatile Map<String,Object> cachedDiffResult;
    private volatile long cachedDiffAt = 0L;

    // 基础健康检查（保持路径 /api/health）
    @GetMapping
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        try {
            jdbcTemplate.execute("SELECT 1");
            health.put("database", "UP");
            // 简单表存在性（默认数据源当前库）
            Map<String, String> core = checkCoreTablesCurrent();
            health.put("coreTables", core);
            health.put("status", core.values().contains("MISSING") ? "DEGRADED" : "UP");
            health.put("code", 200);
            health.put("message", "系统运行" + ("UP".equals(health.get("status")) ? "正常" : "存在缺失表"));
            return ResponseEntity.ok(health);
        } catch (Exception e) {
            health.put("status", "DOWN");
            health.put("code", 503);
            health.put("message", "系统异常: " + e.getMessage());
            health.put("database", "DOWN");
            return ResponseEntity.status(503).body(health);
        }
    }

    // 多租户 Schema 深度检查 /api/health/schema
    @GetMapping("/schema")
    public ResponseEntity<Map<String, Object>> schemaHealth() {
        long ttl = DEFAULT_TTL_MS;
        if (System.currentTimeMillis() - cachedSchemaAt < ttl && cachedSchemaResult != null) {
            return ResponseEntity.ok(cachedSchemaResult);
        }
        Map<String, Object> result = new HashMap<>();
        List<String> required = Arrays.asList("member", "category", "role", "employee", "medicine", "inventory", "order", "order_item", "supplier", "stock_in", "stock_in_item");
        Map<String, javax.sql.DataSource> dsMap = multiTenantDataSourceConfig.getDataSourceMap();
        Map<String, Object> tenants = new LinkedHashMap<>();
        for (var entry : dsMap.entrySet()) {
            String tenantId = entry.getKey();
            javax.sql.DataSource ds = entry.getValue();
            Map<String, Object> tenantInfo = new LinkedHashMap<>();
            List<String> missing = new ArrayList<>();
            try (java.sql.Connection conn = ds.getConnection()) {
                String catalog = conn.getCatalog();
                for (String tbl : required) {
                    if (!tableExists(conn, catalog, tbl)) missing.add(tbl);
                }
                tenantInfo.put("catalog", catalog);
                tenantInfo.put("missingTables", missing);
                tenantInfo.put("status", missing.isEmpty() ? "OK" : "INCOMPLETE");
            } catch (Exception ex) {
                tenantInfo.put("error", ex.getMessage());
                tenantInfo.put("status", "ERROR");
            }
            tenants.put(tenantId, tenantInfo);
        }
        result.put("tenants", tenants);
        boolean allOk = tenants.values().stream().allMatch(v -> "OK".equals(((Map<?, ?>) v).get("status")));
        result.put("overall", allOk ? "UP" : "DEGRADED");
        result.put("timestamp", java.time.LocalDateTime.now().toString());
        cachedSchemaResult = result;
        cachedSchemaAt = System.currentTimeMillis();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/diff")
    public ResponseEntity<Map<String,Object>> schemaDiff(){
        long ttl = DEFAULT_TTL_MS;
        if (System.currentTimeMillis() - cachedDiffAt < ttl && cachedDiffResult != null) {
            return ResponseEntity.ok(cachedDiffResult);
        }
        Map<String,Object> out = new LinkedHashMap<>();
        Map<String, javax.sql.DataSource> dsMap = multiTenantDataSourceConfig.getDataSourceMap();
        String baseTenant = dsMap.containsKey("rzt")?"rzt":"default";
        javax.sql.DataSource baseDs = dsMap.get(baseTenant);
        List<String> tables = Arrays.asList("member","category","role","employee","medicine","inventory","order","order_item","supplier","stock_in","stock_in_item");
        Map<String,Map<String,Object>> diffPerTenant = new LinkedHashMap<>();
        Map<String,Map<String,String>> baseTableCols = new HashMap<>();
        try(java.sql.Connection baseConn = baseDs.getConnection()){
            String baseCatalog = baseConn.getCatalog();
            for(String t: tables){
                baseTableCols.put(t, fetchColumns(baseConn, baseCatalog, t));
            }
        } catch(Exception ex){
            out.put("error","Base tenant read failed: "+ex.getMessage());
        }
        for(var e: dsMap.entrySet()){
            String tenantId = e.getKey();
            javax.sql.DataSource ds = e.getValue();
            if(tenantId.equals(baseTenant)) continue;
            Map<String,Object> tInfo = new LinkedHashMap<>();
            List<Map<String,Object>> tableDiffs = new ArrayList<>();
            try(java.sql.Connection c = ds.getConnection()){
                String catalog = c.getCatalog();
                for(String t: tables){
                    Map<String,String> baseCols = baseTableCols.getOrDefault(t, Collections.emptyMap());
                    Map<String,String> cols = fetchColumns(c, catalog, t);
                    if(cols.isEmpty()){
                        tableDiffs.add(Map.of("table",t,"status","MISSING"));
                        continue;
                    }
                    List<String> missing = new ArrayList<>();
                    List<String> typeMismatch = new ArrayList<>();
                    for(var bc: baseCols.entrySet()){
                        String col = bc.getKey();
                        String bType = bc.getValue();
                        String tType = cols.get(col);
                        if(tType==null) missing.add(col); else if(!normalizeType(bType).equals(normalizeType(tType))) typeMismatch.add(col+":"+tType+"!="+bType);
                    }
                    Map<String,Object> one = new LinkedHashMap<>();
                    one.put("table", t);
                    one.put("missingColumns", missing);
                    one.put("typeMismatch", typeMismatch);
                    one.put("status", missing.isEmpty()&&typeMismatch.isEmpty()?"OK":"DIFF");
                    tableDiffs.add(one);
                }
            } catch(Exception ex){
                tInfo.put("error", ex.getMessage());
            }
            tInfo.put("tables", tableDiffs);
            diffPerTenant.put(tenantId, tInfo);
        }
        out.put("base", baseTenant);
        out.put("diff", diffPerTenant);
        out.put("timestamp", java.time.LocalDateTime.now().toString());
        cachedDiffResult = out; cachedDiffAt = System.currentTimeMillis();
        return ResponseEntity.ok(out);
    }

    private Map<String,String> fetchColumns(java.sql.Connection conn,String catalog,String table){
        Map<String,String> map = new LinkedHashMap<>();
        try(java.sql.Statement st = conn.createStatement(); java.sql.ResultSet rs = st.executeQuery("SELECT COLUMN_NAME, COLUMN_TYPE FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='"+catalog+"' AND TABLE_NAME='"+table+"'")){
            while(rs.next()){ map.put(rs.getString(1), rs.getString(2)); }
        } catch(Exception ignored){}
        return map;
    }
    private String normalizeType(String t){
        if(t==null) return ""; return t.toLowerCase().replace(" unsigned"," ").trim();
    }

    private Map<String, String> checkCoreTablesCurrent() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("order", exists("SELECT COUNT(*) FROM `order`") ? "EXISTS" : "MISSING");
        map.put("order_item", exists("SELECT COUNT(*) FROM order_item") ? "EXISTS" : "MISSING");
        map.put("member", exists("SELECT COUNT(*) FROM member") ? "EXISTS" : "MISSING");
        map.put("medicine", exists("SELECT COUNT(*) FROM medicine") ? "EXISTS" : "MISSING");
        return map;
    }

    private boolean exists(String sql) {
        try {
            jdbcTemplate.queryForObject(sql, Integer.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean tableExists(java.sql.Connection conn, String catalog, String table) {
        try (java.sql.ResultSet rs = conn.getMetaData().getTables(catalog, null, table, null)) {
            return rs.next();
        } catch (Exception e) {
            return false;
        }
    }
}

