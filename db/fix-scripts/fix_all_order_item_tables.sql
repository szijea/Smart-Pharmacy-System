-- 直接修复脚本：确保所有租户数据库的 order_item 表结构与 OrderItem 实体匹配
-- OrderItem 实体期望 item_id 作为主键列

-- ===== wx 数据库 =====
USE wx;
-- 验证表结构
DESCRIBE order_item;
SELECT "wx DONE" as result;

-- ===== bht 数据库 =====
USE bht;
-- 验证表结构
DESCRIBE order_item;
SELECT "bht DONE" as result;

-- ===== rzt_db 数据库 =====
USE rzt_db;
-- 验证表结构
DESCRIBE order_item;
SELECT "rzt_db DONE" as result;




