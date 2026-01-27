-- 添加 barcode 与 production_date 字段到 medicine 表
-- 适用 MySQL 8+，包含 IF NOT EXISTS，重复执行不会报错。
-- 请在执行前确认当前数据库为 rzt_db，或使用 USE rzt_db;

ALTER TABLE medicine
  ADD COLUMN IF NOT EXISTS barcode VARCHAR(64) NULL COMMENT '条形码',
  ADD COLUMN IF NOT EXISTS production_date DATE NULL COMMENT '生产日期';

-- 可选：为条形码建立唯一索引（若业务要求每个条形码唯一）
-- CREATE UNIQUE INDEX idx_medicine_barcode ON medicine (barcode);

-- 可选：为生产日期建立普通索引（如果会按生产日期查询/统计）
-- CREATE INDEX idx_medicine_production_date ON medicine (production_date);

-- 验证：
-- DESC medicine;
-- 或 SELECT medicine_id, barcode, production_date FROM medicine LIMIT 5;

-- 回滚（删除列）示例：
-- ALTER TABLE medicine DROP COLUMN barcode;
-- ALTER TABLE medicine DROP COLUMN production_date;

