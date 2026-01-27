-- Add status column to medicine table if missing
ALTER TABLE medicine ADD COLUMN status VARCHAR(20) NULL COMMENT '销售状态';
UPDATE medicine SET status='ACTIVE' WHERE status IS NULL;
-- Verify
SELECT medicine_id, approval_no, status FROM medicine LIMIT 10;

