# 修复说明：order_item 表 JDBC 异常 - item_id 主键问题

## 问题根源

用户看到错误：
```
JDBC exception executing SQL [select oi1_0.id,oi1_0.medicine_id,...] 
[Unknown column 'oi1_0.id' in 'field list']
```

根本原因是：
- 数据库表 `order_item` 中的主键列名为 `item_id`
- 但 `OrderItem` 实体类中 `@Column` 注解错误地指定为 `"id"`
- 导致 Hibernate 生成的 SQL 查询使用错误的列名

## 解决方案

### 修改 OrderItem 实体（已完成）

文件：[src/main/java/com/pharmacy/entity/OrderItem.java](src/main/java/com/pharmacy/entity/OrderItem.java)

```java
@Entity
@Table(name = "order_item")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "item_id")  // ← 改为 "item_id" 而不是 "id"
    private Long itemId;
    ...
}
```

### 更新迁移脚本（已完成）

[src/main/resources/db/migration/V6__add_order_item_id_column.sql](src/main/resources/db/migration/V6__add_order_item_id_column.sql)
- 删除旧的 `id` 列（如果存在）
- 确保 `item_id` 是正确的主键

### 更新 AutoSchemaPatch（已完成）

[src/main/java/com/pharmacy/config/AutoSchemaPatch.java](src/main/java/com/pharmacy/config/AutoSchemaPatch.java)
- 应用启动时自动检测 `item_id` 是否为主键
- 如果不是，自动修复

## 验证

所有数据库的 order_item 表结构均已验证：
```
mysql> DESCRIBE order_item;
+-----------------+---------------+------+-----+---------+-------
----+
| Field           | Type          | Null | Key | Default | Extra 
|
+-----------------+---------------+------+-----+---------+-------
----+
| item_id         | int(11)       | NO   | PRI | NULL    | auto_
increment |
| order_id        | varchar(32)   | NO   | MUL | NULL    |      
|
| medicine_id     | varchar(32)   | NO   | MUL | NULL    |      
|
| quantity        | int(11)       | NO   |     | NULL    |      
|
| unit_price      | decimal(10,2) | NO   |     | NULL    |      
|
| subtotal        | decimal(10,2) | NO   |     | NULL    |      
|
| prescription_id | varchar(32)   | YES  |     | NULL    |      
|
+-----------------+---------------+------+-----+---------+-------
----+
```

✓ wx 数据库：OK
✓ bht 数据库：OK  
✓ rzt_db 数据库：OK

## 后续步骤

1. **重新编译应用**
   ```bash
   mvn clean compile
   ```

2. **重启应用**
   - 应用启动时会自动执行 AutoSchemaPatch 修复
   - 日志会显示修复结果

3. **测试订单功能**
   - 创建新订单
   - 查看订单详情
   - 订单历史列表

## 相关改动

- `src/main/java/com/pharmacy/entity/OrderItem.java` - 实体主键列名修正
- `src/main/java/com/pharmacy/config/AutoSchemaPatch.java` - 自动修复逻辑
- `src/main/resources/db/migration/V6__add_order_item_id_column.sql` - Flyway 迁移脚本
- `db/fix-scripts/fix_all_order_item_tables.sql` - 验证和修复脚本
