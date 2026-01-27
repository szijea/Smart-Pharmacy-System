-- convert_remaining_tables.sql
-- 补充脚本：转换遗漏的表为 utf8mb4_unicode_ci
-- 这些表在前面的脚本中因为执行流程导致 CONVERT 未完成

-- 1) DROP FKs that block CONVERT for order_item -> order
-- These may have been dropped already but try anyway (ignore errors)
ALTER TABLE wx.order_item DROP FOREIGN KEY fk_item_order;
ALTER TABLE bht.order_item DROP FOREIGN KEY fk_item_order;
ALTER TABLE rzt_db.order_item DROP FOREIGN KEY fk_item_order;

ALTER TABLE wx.order_item DROP FOREIGN KEY fk_order_item_order;
ALTER TABLE bht.order_item DROP FOREIGN KEY fk_order_item_order;
ALTER TABLE rzt_db.order_item DROP FOREIGN KEY fk_order_item_order;

-- 2) Convert hang_order (parent table of hang_order_item)
ALTER TABLE wx.hang_order CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE bht.hang_order CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE rzt_db.hang_order CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 3) Convert hang_order_item
ALTER TABLE wx.hang_order_item CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE bht.hang_order_item CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE rzt_db.hang_order_item CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 4) Convert order (parent table of order_item) - MUST do this before converting order_item
ALTER TABLE wx.order CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE bht.order CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE rzt_db.order CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 5) Convert order_item
ALTER TABLE wx.order_item CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE bht.order_item CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE rzt_db.order_item CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 6) Convert stock_alert
ALTER TABLE wx.stock_alert CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE bht.stock_alert CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE rzt_db.stock_alert CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 7) Recreate FKs for order_item -> order
ALTER TABLE wx.order_item ADD CONSTRAINT fk_item_order FOREIGN KEY (order_id) REFERENCES wx.order(order_id) ON DELETE RESTRICT;
ALTER TABLE bht.order_item ADD CONSTRAINT fk_item_order FOREIGN KEY (order_id) REFERENCES bht.order(order_id) ON DELETE RESTRICT;
ALTER TABLE rzt_db.order_item ADD CONSTRAINT fk_item_order FOREIGN KEY (order_id) REFERENCES rzt_db.order(order_id) ON DELETE RESTRICT;

ALTER TABLE wx.order_item ADD CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES wx.order(order_id) ON DELETE RESTRICT;
ALTER TABLE bht.order_item ADD CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES bht.order(order_id) ON DELETE RESTRICT;
ALTER TABLE rzt_db.order_item ADD CONSTRAINT fk_order_item_order FOREIGN KEY (order_id) REFERENCES rzt_db.order(order_id) ON DELETE RESTRICT;

-- 8) Verification: Check all medicine_id columns are now consistent
SELECT TABLE_SCHEMA, TABLE_NAME, COLUMN_NAME, COLUMN_TYPE, CHARACTER_SET_NAME, COLLATION_NAME
FROM information_schema.COLUMNS
WHERE TABLE_SCHEMA IN ('wx','bht','rzt_db') AND COLUMN_NAME='medicine_id'
ORDER BY TABLE_SCHEMA, TABLE_NAME;

