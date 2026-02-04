# 修复说明：order_item 表 id 列缺失问题

## 问题

用户在查看订单详情时出现 JDBC 异常：
```
Unknown column 'oi1_0.id' in 'field list'
```

这是因为现有的生产数据库中 `order_item` 表缺少 `id` 列（主键）。

## 解决方案

### 1. 自动修复（推荐）

应用启动时会自动检测并修复此问题。修复逻辑在 `AutoSchemaPatch` 类中：
- 检查 `order_item` 表是否存在 `id` 列
- 如果不存在，自动删除旧主键约束并添加 `id` 列作为自增主键

启动日志示例：
```
[AUTO SCHEMA PATCH] 开始检测 order_item 表...
[AUTO SCHEMA PATCH] order_item 表缺少 id 列，开始修复...
[AUTO SCHEMA PATCH] 已删除 order_item 表的主键约束
[AUTO SCHEMA PATCH] 成功为 order_item 表添加 id 列
```

### 2. 手动修复

如果需要手动执行，可运行以下脚本：

```bash
# 修复单个数据库
mysql -u root -p wx < db/fix-scripts/fix_order_item_id_column.sql
mysql -u root -p bht < db/fix-scripts/fix_order_item_id_column.sql
mysql -u root -p rzt_db < db/fix-scripts/fix_order_item_id_column.sql
```

或使用 PowerShell 脚本自动化修复所有数据库：
```powershell
powershell -ExecutionPolicy Bypass -File db/fix-scripts/fix_order_item_id_all.ps1
```

### 3. 验证修复

修复后，可以验证表结构：
```sql
DESCRIBE order_item;

-- 或查看是否有 id 列
SHOW COLUMNS FROM order_item;
```

## 相关文件

- `src/main/java/com/pharmacy/config/AutoSchemaPatch.java` - 自动修复逻辑
- `src/main/resources/db/migration/V6__add_order_item_id_column.sql` - Flyway 迁移脚本
- `src/main/resources/db/migration/V7__fix_medicine_stock_quantity.sql` - 库存修复脚本
- `db/fix-scripts/fix_order_item_id_column.sql` - 手动修复脚本
- `db/fix-scripts/fix_order_item_id_all.ps1` - PowerShell 自动化脚本
- `src/main/resources/application.yaml` - 启用了 Flyway

## 改进

1. **搜索框自动触发** - 药品名称和条码输入框现在无需点击"搜索"按钮即可自动弹出结果
2. **JDBC 异常修复** - 自动为缺失 id 列的表添加主键
3. **库存补齐** - 确保测试药品有足够库存供测试使用
