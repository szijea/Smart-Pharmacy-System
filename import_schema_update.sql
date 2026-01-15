-- Modify medicine table to support import fields
ALTER TABLE `medicine`
    ADD COLUMN IF NOT EXISTS `product_code` VARCHAR(50) COMMENT '商品货号' AFTER `medicine_id`,
    ADD COLUMN IF NOT EXISTS `dosage_form` VARCHAR(50) COMMENT '剂型' AFTER `trade_name`,
    ADD COLUMN IF NOT EXISTS `barcode` VARCHAR(64) COMMENT '条码' AFTER `dosage_form`,
    ADD COLUMN IF NOT EXISTS `production_date` DATE COMMENT '生产日期' AFTER `barcode`,
    ADD COLUMN IF NOT EXISTS `expiry_date` DATE COMMENT '有效期' AFTER `production_date`,
    ADD COLUMN IF NOT EXISTS `status` VARCHAR(20) COMMENT '状态' AFTER `expiry_date`,
    ADD COLUMN IF NOT EXISTS `usage_dosage` VARCHAR(255) COMMENT '用法用量' AFTER `status`,
    ADD COLUMN IF NOT EXISTS `contraindication` VARCHAR(255) COMMENT '禁忌症' AFTER `usage_dosage`,
    ADD COLUMN IF NOT EXISTS `manufacturer` VARCHAR(100) COMMENT '生产厂家';

-- Make category_id nullable to allow import without category initially (optional, but safer for import)
-- Note: modifying column with FK constraint might require dropping FK first in some strict modes, but usually MODIFY COLUMN works if types match.
-- Checking rzt.sql: category_id is INT NOT NULL.
ALTER TABLE `medicine` MODIFY COLUMN `category_id` INT NULL COMMENT '所属分类ID（关联category表）';

-- Modify inventory table to support import fields
ALTER TABLE `inventory`
    ADD COLUMN IF NOT EXISTS `warehouse` VARCHAR(50) COMMENT '仓库' AFTER `stock_quantity`,
    ADD COLUMN IF NOT EXISTS `supplier_name` VARCHAR(100) COMMENT '供货单位' AFTER `warehouse`,
    ADD COLUMN IF NOT EXISTS `inbound_date` DATETIME COMMENT '入库日期' AFTER `supplier_name`,
    ADD COLUMN IF NOT EXISTS `acceptance_no` VARCHAR(50) COMMENT '验收单号' AFTER `inbound_date`,
    ADD COLUMN IF NOT EXISTS `invoice_no` VARCHAR(50) COMMENT '发票号' AFTER `acceptance_no`,
    ADD COLUMN IF NOT EXISTS `invoice_date` DATETIME COMMENT '发票日期' AFTER `invoice_no`,
    ADD COLUMN IF NOT EXISTS `acceptance_date` DATETIME COMMENT '验收日期' AFTER `invoice_date`,
    ADD COLUMN IF NOT EXISTS `status` VARCHAR(20) COMMENT '状态' AFTER `acceptance_date`;
