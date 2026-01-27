-- 添加 medicine 表缺失的 contraindication 列
-- 注意：如果列已存在，此语句可能会失败。在 MySQL 8.0+ 可以使用 ADD COLUMN IF NOT EXISTS，但在旧版本不行。
-- 这里假设列确实缺失。
ALTER TABLE medicine ADD COLUMN contraindication VARCHAR(255);

