-- bht_fix.sql
-- 说明：在对生产库执行之前请先备份并在测试环境验证。
-- 备份命令示例（PowerShell）：
-- mysqldump -uroot -p --single-transaction --set-gtid-purged=OFF bht > .\bht_backup_$(Get-Date -Format yyyyMMddHHmmss).sql

-- 1) 检测可能的不兼容外键（仅输出语句，先 REVIEW）
SELECT CONCAT('/* DROP FK IF NEEDED */ ALTER TABLE `',k.TABLE_SCHEMA,'`.`',k.TABLE_NAME,'` DROP FOREIGN KEY `',k.CONSTRAINT_NAME,'`;') AS stmt,
       k.TABLE_SCHEMA,k.TABLE_NAME,k.CONSTRAINT_NAME,
       cc.DATA_TYPE AS referencing_data_type, cc.COLLATION_NAME AS referencing_collation,
       rc.DATA_TYPE AS referenced_data_type, rc.COLLATION_NAME AS referenced_collation
FROM information_schema.KEY_COLUMN_USAGE k
JOIN information_schema.COLUMNS rc ON rc.TABLE_SCHEMA=k.REFERENCED_TABLE_SCHEMA AND rc.TABLE_NAME=k.REFERENCED_TABLE_NAME AND rc.COLUMN_NAME=k.REFERENCED_COLUMN_NAME
JOIN information_schema.COLUMNS cc ON cc.TABLE_SCHEMA=k.TABLE_SCHEMA AND cc.TABLE_NAME=k.TABLE_NAME AND cc.COLUMN_NAME=k.COLUMN_NAME
WHERE k.REFERENCED_TABLE_NAME='medicine' AND k.REFERENCED_COLUMN_NAME='medicine_id'
  AND k.TABLE_SCHEMA='bht'
  AND (cc.DATA_TYPE<>rc.DATA_TYPE OR IFNULL(cc.COLLATION_NAME,'')<>IFNULL(rc.COLLATION_NAME,''));

-- 2) 导出孤儿样本（如有）以便人工判断（示例：stock_in_item）
SELECT c.* FROM bht.stock_in_item c LEFT JOIN bht.medicine p ON CAST(c.medicine_id AS CHAR) = p.medicine_id WHERE c.medicine_id IS NOT NULL AND p.medicine_id IS NULL LIMIT 200;

-- 3) 如确定可以删除孤儿，示例删除语句（请先备份）
-- DELETE c FROM bht.stock_in_item c LEFT JOIN bht.medicine p ON CAST(c.medicine_id AS CHAR) = p.medicine_id WHERE c.medicine_id IS NOT NULL AND p.medicine_id IS NULL;

-- 4) DROP existing FKs that reference medicine (to avoid 3780 error during CONVERT)
-- Note: Will show error if FK doesn't exist, but won't stop execution
ALTER TABLE bht.inventory DROP FOREIGN KEY fk_inventory_medicine;
ALTER TABLE bht.order_item DROP FOREIGN KEY fk_item_medicine;
ALTER TABLE bht.order_item DROP FOREIGN KEY fk_order_item_medicine;
ALTER TABLE bht.order_item DROP FOREIGN KEY fk_item_order;
ALTER TABLE bht.order_item DROP FOREIGN KEY fk_order_item_order;
ALTER TABLE bht.stock_alert DROP FOREIGN KEY fk_alert_medicine;
ALTER TABLE bht.stock_record DROP FOREIGN KEY fk_stock_medicine;

-- 5) 将引用方的 numeric 类型改为 VARCHAR(32)（按检测结果执行）
ALTER TABLE bht.stock_in_item MODIFY COLUMN medicine_id VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL;
ALTER TABLE bht.sale_record MODIFY COLUMN medicine_id VARCHAR(32) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL;

-- 6) 将存在不一致 COLLATION 的 varchar 列强制指定为 utf8mb4_unicode_ci
ALTER TABLE bht.stock MODIFY COLUMN medicine_id VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL;

-- 7) 将关键表转换为目标字符集/排序（现在不会触发 3780）
ALTER TABLE bht.medicine CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE bht.order CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE bht.inventory CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE bht.order_item CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE bht.stock_record CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE bht.stock_alert CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE bht.stock_in_item CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 7) 为可能缺失的索引创建索引（外键创建前应有索引）
CREATE INDEX IF NOT EXISTS idx_stock_in_item_med ON bht.stock_in_item(medicine_id);
CREATE INDEX IF NOT EXISTS idx_stock_med ON bht.stock(medicine_id);

-- 8) 重建之前 DROP 的外键
ALTER TABLE bht.inventory ADD CONSTRAINT fk_inventory_medicine FOREIGN KEY (medicine_id) REFERENCES bht.medicine(medicine_id) ON DELETE RESTRICT;
ALTER TABLE bht.order_item ADD CONSTRAINT fk_item_medicine FOREIGN KEY (medicine_id) REFERENCES bht.medicine(medicine_id) ON DELETE RESTRICT;
ALTER TABLE bht.order_item ADD CONSTRAINT fk_order_item_medicine FOREIGN KEY (medicine_id) REFERENCES bht.medicine(medicine_id) ON DELETE RESTRICT;
ALTER TABLE bht.order_item ADD CONSTRAINT fk_item_order FOREIGN KEY (order_id) REFERENCES bht.order(order_id) ON DELETE RESTRICT;
ALTER TABLE bht.order_item ADD CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES bht.order(order_id) ON DELETE RESTRICT;
ALTER TABLE bht.stock_alert ADD CONSTRAINT fk_alert_medicine FOREIGN KEY (medicine_id) REFERENCES bht.medicine(medicine_id) ON DELETE RESTRICT;
ALTER TABLE bht.stock_record ADD CONSTRAINT fk_stock_medicine FOREIGN KEY (medicine_id) REFERENCES bht.medicine(medicine_id) ON DELETE RESTRICT;
ALTER TABLE bht.stock_in_item ADD CONSTRAINT fk_stock_item_medicine FOREIGN KEY (medicine_id) REFERENCES bht.medicine(medicine_id) ON DELETE RESTRICT;

-- 9) 验证：确认列类型/排序一致，以及外键存在
SELECT TABLE_NAME, COLUMN_NAME, COLUMN_TYPE, DATA_TYPE, CHARACTER_SET_NAME, COLLATION_NAME FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='bht' AND COLUMN_NAME='medicine_id' ORDER BY TABLE_NAME;
SELECT TABLE_NAME, CONSTRAINT_NAME FROM information_schema.KEY_COLUMN_USAGE WHERE REFERENCED_TABLE_NAME='medicine' AND REFERENCED_COLUMN_NAME='medicine_id' AND TABLE_SCHEMA='bht';

