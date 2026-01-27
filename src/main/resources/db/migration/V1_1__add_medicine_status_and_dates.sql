-- Flyway migration: add missing medicine columns (idempotent for MySQL 8+)
ALTER TABLE medicine ADD COLUMN IF NOT EXISTS status VARCHAR(20) NULL COMMENT '销售状态';
ALTER TABLE medicine ADD COLUMN IF NOT EXISTS barcode VARCHAR(64) NULL COMMENT '条形码';
ALTER TABLE medicine ADD COLUMN IF NOT EXISTS production_date DATE NULL COMMENT '生产日期';
ALTER TABLE medicine ADD COLUMN IF NOT EXISTS expiry_date DATE NULL COMMENT '到期日期';
UPDATE medicine SET status='ACTIVE' WHERE status IS NULL;
