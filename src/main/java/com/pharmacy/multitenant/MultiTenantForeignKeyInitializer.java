package com.pharmacy.multitenant;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.*;
import java.util.*;

@Configuration
public class MultiTenantForeignKeyInitializer {

    static class FKSpec {
        String table; String name; String column; String refTable; String refColumn; String onDelete; boolean createIndex;
        FKSpec(String table,String name,String column,String refTable,String refColumn,String onDelete, boolean createIndex){
            this.table=table; this.name=name; this.column=column; this.refTable=refTable; this.refColumn=refColumn; this.onDelete=onDelete; this.createIndex=createIndex; }
    }

    @Bean
    public ApplicationRunner foreignKeyRunner(@Qualifier("tenantDataSources") Map<String, DataSource> tenantDataSources) {
        return args -> {
            System.out.println("[FKInit] 开始检测并补充外键...");
            List<FKSpec> specs = List.of(
                    new FKSpec("order_item","fk_order_item_order","order_id","order","order_id","CASCADE", true),
                    new FKSpec("order_item","fk_order_item_medicine","medicine_id","medicine","medicine_id","RESTRICT", true),
                    new FKSpec("medicine","fk_medicine_category","category_id","category","category_id","RESTRICT", true),
                    new FKSpec("inventory","fk_inventory_medicine","medicine_id","medicine","medicine_id","RESTRICT", true),
                    new FKSpec("stock_in_item","fk_stock_item_stock_in","stock_in_id","stock_in","stock_in_id","CASCADE", true),
                    new FKSpec("stock_in_item","fk_stock_item_medicine","medicine_id","medicine","medicine_id","RESTRICT", true)
            );
            for(var entry : tenantDataSources.entrySet()){
                String tenantId = entry.getKey();
                try(Connection conn = entry.getValue().getConnection()){
                    String catalog = conn.getCatalog();
                    for(FKSpec fk : specs){
                        if(!tableExists(conn, catalog, fk.table) || !tableExists(conn, catalog, fk.refTable)) continue;
                        if(foreignKeyExists(conn, catalog, fk.table, fk.name)) continue; // already exists
                        if(hasOrphans(conn, catalog, fk)){
                            System.err.println("[FKInit] 跳过外键 "+fk.name+" (租户="+tenantId+") 因存在孤儿记录");
                            continue;
                        }
                        if(fk.createIndex && !indexExists(conn, catalog, fk.table, fk.column)){
                            try(Statement st = conn.createStatement()){
                                st.executeUpdate("CREATE INDEX idx_"+fk.table+"_"+fk.column+" ON "+quote(fk.table)+"("+fk.column+")");
                                System.out.println("[FKInit] 已创建索引 idx_"+fk.table+"_"+fk.column+" (租户="+tenantId+")");
                            } catch(SQLException ex){
                                System.err.println("[FKInit] 创建索引失败 "+fk.table+"."+fk.column+" 租户="+tenantId+" msg="+ex.getMessage());
                            }
                        }
                        String ddl = "ALTER TABLE "+quote(fk.table)+" ADD CONSTRAINT "+fk.name+" FOREIGN KEY ("+fk.column+") REFERENCES "+quote(fk.refTable)+"("+fk.refColumn+") ON DELETE "+fk.onDelete;
                        try(Statement st = conn.createStatement()){
                            st.executeUpdate(ddl);
                            System.out.println("[FKInit] 已添加外键 "+fk.name+" (租户="+tenantId+")");
                        } catch(SQLException ex){
                            System.err.println("[FKInit] 添加外键失败 "+fk.name+" (租户="+tenantId+") msg="+ex.getMessage());
                        }
                    }
                } catch(Exception ex){
                    System.err.println("[FKInit] 租户="+tenantId+" 外键初始化失败: "+ex.getMessage());
                }
            }
            System.out.println("[FKInit] 外键检测与补充完成");
        };
    }

    private boolean tableExists(Connection conn,String catalog,String table) throws SQLException {
        try(ResultSet rs = conn.getMetaData().getTables(catalog,null,table,null)){ return rs.next(); }
    }
    private boolean foreignKeyExists(Connection conn,String catalog,String table,String fkName) throws SQLException {
        String sql = "SELECT CONSTRAINT_NAME FROM information_schema.TABLE_CONSTRAINTS WHERE TABLE_SCHEMA=? AND TABLE_NAME=? AND CONSTRAINT_TYPE='FOREIGN KEY' AND CONSTRAINT_NAME=?";
        try(PreparedStatement ps = conn.prepareStatement(sql)){ ps.setString(1,catalog); ps.setString(2,table); ps.setString(3,fkName); try(ResultSet rs = ps.executeQuery()){ return rs.next(); }}
    }
    private boolean indexExists(Connection conn,String catalog,String table,String column) throws SQLException {
        String sql = "SELECT INDEX_NAME FROM information_schema.STATISTICS WHERE TABLE_SCHEMA=? AND TABLE_NAME=? AND COLUMN_NAME=?";
        try(PreparedStatement ps = conn.prepareStatement(sql)){ ps.setString(1,catalog); ps.setString(2,table); ps.setString(3,column); try(ResultSet rs = ps.executeQuery()){ return rs.next(); }}
    }
    private boolean hasOrphans(Connection conn,String catalog, FKSpec fk) throws SQLException {
        String sql = "SELECT 1 FROM "+quote(fk.table)+" c LEFT JOIN "+quote(fk.refTable)+" p ON c."+fk.column+"=p."+fk.refColumn+" WHERE c."+fk.column+" IS NOT NULL AND p."+fk.refColumn+" IS NULL LIMIT 1";
        try(Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)){ return rs.next(); }
    }
    private String quote(String name){ return "`"+name+"`"; }
}

