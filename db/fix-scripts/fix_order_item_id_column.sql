-- 修复脚本：确保 order_item 表的 item_id 列是正确的主键
-- OrderItem 实体使用 @Column(name = "item_id") 作为主键

-- 删除可能存在的旧 id 列（如果有的话）
ALTER TABLE order_item DROP COLUMN IF EXISTS id;

-- 删除现有的主键约束
ALTER TABLE order_item DROP PRIMARY KEY;

-- 添加或重置 item_id 为自增主键
ALTER TABLE order_item 
ADD COLUMN item_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY FIRST;

-- 验证修复
DESCRIBE order_item;

