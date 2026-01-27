-- 迁移：为当前租户库的 `order` 表补齐与 JPA Order 实体一致的新增列
-- 缺失列：customer_name VARCHAR(100)、refund_time DATETIME、refund_reason VARCHAR(200)
-- 在多租户模式下，Flyway 会分别对每个数据源执行本脚本，因此不可使用 USE 语句。
-- 如果某列已存在会报 Duplicate column name，可忽略或手动拆分执行。

ALTER TABLE `order`
  ADD COLUMN customer_name VARCHAR(100) NULL COMMENT '客户姓名' AFTER member_id,
  ADD COLUMN refund_time DATETIME NULL COMMENT '退款时间' AFTER pay_time,
  ADD COLUMN refund_reason VARCHAR(200) NULL COMMENT '退款原因' AFTER refund_time;
