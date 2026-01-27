-- 扩展 medicine 表以匹配当前 JPA Entity 字段 (deleted, status, barcode, production_date, expiry_date)
-- 请在 MySQL 中逐条或整段执行；若某列已存在，可忽略报错。

ALTER TABLE medicine
  ADD COLUMN IF NOT EXISTS status VARCHAR(20) NULL COMMENT '销售/显示状态: ACTIVE/INACTIVE' AFTER update_time,
  ADD COLUMN IF NOT EXISTS deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标记: 0=未删除,1=已删除' AFTER status,
  ADD COLUMN IF NOT EXISTS barcode VARCHAR(64) NULL COMMENT '条形码' AFTER deleted,
  ADD COLUMN IF NOT EXISTS production_date DATE NULL COMMENT '生产日期' AFTER barcode,
  ADD COLUMN IF NOT EXISTS expiry_date DATE NULL COMMENT '到期日期' AFTER production_date;

-- 注意：MySQL 低版本不支持 IF NOT EXISTS 多列写法，若执行报 1064 语法错误，请改为逐条：
-- ALTER TABLE medicine ADD COLUMN status VARCHAR(20) NULL COMMENT '销售/显示状态: ACTIVE/INACTIVE' AFTER update_time;
-- ALTER TABLE medicine ADD COLUMN deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标记: 0=未删除,1=已删除' AFTER status;
-- ALTER TABLE medicine ADD COLUMN barcode VARCHAR(64) NULL COMMENT '条形码' AFTER deleted;
-- ALTER TABLE medicine ADD COLUMN production_date DATE NULL COMMENT '生产日期' AFTER barcode;
-- ALTER TABLE medicine ADD COLUMN expiry_date DATE NULL COMMENT '到期日期' AFTER production_date;

-- 为常用查询添加索引（若不存在）
ALTER TABLE medicine ADD INDEX idx_status (status);
ALTER TABLE medicine ADD INDEX idx_deleted (deleted);
ALTER TABLE medicine ADD INDEX idx_expiry (expiry_date);

-- 验证：
-- DESC medicine;
-- SELECT medicine_id, approval_no, status, deleted, barcode, production_date, expiry_date FROM medicine LIMIT 5;
