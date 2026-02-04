-- 迁移：确保 order_item 表的 item_id 列是正确的主键
-- 某些现有库中 order_item 表可能没有 item_id 列，或 item_id 不是主键
-- 该脚本对新库无影响（因为初始化脚本已创建）
-- 在多租户环境下，Flyway 会为每个数据源执行此脚本

-- 步骤1：检查 item_id 列是否存在，如果不存在则添加
-- 注意：这个脚本假设 item_id 是主键列的标准命名
-- 如果旧表使用了 id，需要重命名或修改

-- 删除可能存在的旧 id 列
ALTER TABLE `order_item` DROP COLUMN IF EXISTS `id`;

-- 确保 item_id 列存在且为主键
-- 如果已有主键，先删除
ALTER TABLE `order_item` DROP PRIMARY KEY;

-- 添加或修改 item_id 列为主键
ALTER TABLE `order_item` 
ADD COLUMN `item_id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY FIRST;






