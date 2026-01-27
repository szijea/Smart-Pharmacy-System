-- Flyway migration: add soft delete column
ALTER TABLE medicine ADD COLUMN IF NOT EXISTS deleted TINYINT(1) NOT NULL DEFAULT 0 COMMENT '软删除标记 0=正常 1=已删除';
-- Initialize existing rows
UPDATE medicine SET deleted=0 WHERE deleted IS NULL;

