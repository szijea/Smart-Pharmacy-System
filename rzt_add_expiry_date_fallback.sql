-- 兼容旧版 MySQL 的迁移脚本（逐条添加列，不使用 IF NOT EXISTS）
-- 如果列已存在，执行会报错，可忽略该错误并继续后续列；或先用 information_schema 检测。
-- 建议执行前先: USE rzt_db;

-- 可选：查看当前版本
-- SHOW VARIABLES LIKE 'version';

-- 检查列是否存在示例（手动执行）
-- SELECT COLUMN_NAME FROM information_schema.COLUMNS WHERE TABLE_SCHEMA='rzt_db' AND TABLE_NAME='medicine';

-- 添加条形码列（若不存在）
ALTER TABLE medicine ADD COLUMN barcode VARCHAR(64) NULL COMMENT '条形码';

-- 添加生产日期列（若不存在）
ALTER TABLE medicine ADD COLUMN production_date DATE NULL COMMENT '生产日期';

-- 添加到期日期列（若不存在）
ALTER TABLE medicine ADD COLUMN expiry_date DATE NULL COMMENT '到期日期';

-- 验证
-- DESC medicine;
-- SELECT medicine_id, barcode, production_date, expiry_date FROM medicine LIMIT 5;

-- 回滚示例（谨慎执行）：
-- ALTER TABLE medicine DROP COLUMN expiry_date;
-- ALTER TABLE medicine DROP COLUMN production_date;
-- ALTER TABLE medicine DROP COLUMN barcode;

