-- Fix missing columns in medicine and inventory tables for import functionality
-- Run this script if you encounter "Unknown column" errors during import

USE bht;
-- Medicine Table
ALTER TABLE medicine ADD COLUMN IF NOT EXISTS dosage_form VARCHAR(50) COMMENT '剂型';
ALTER TABLE medicine ADD COLUMN IF NOT EXISTS product_code VARCHAR(50) COMMENT '商品货号';
ALTER TABLE medicine ADD COLUMN IF NOT EXISTS manufacturer VARCHAR(100) COMMENT '生产厂家';
ALTER TABLE medicine ADD COLUMN IF NOT EXISTS usage_dosage VARCHAR(255) COMMENT '用法用量';
ALTER TABLE medicine ADD COLUMN IF NOT EXISTS contraindication VARCHAR(255) COMMENT '禁忌症';
ALTER TABLE medicine ADD COLUMN IF NOT EXISTS barcode VARCHAR(64) COMMENT '条码';

-- Inventory Table
ALTER TABLE inventory ADD COLUMN IF NOT EXISTS warehouse VARCHAR(50) COMMENT '仓库';
ALTER TABLE inventory ADD COLUMN IF NOT EXISTS inbound_date DATETIME COMMENT '入库日期';
ALTER TABLE inventory ADD COLUMN IF NOT EXISTS acceptance_no VARCHAR(50) COMMENT '验收单号';
ALTER TABLE inventory ADD COLUMN IF NOT EXISTS invoice_no VARCHAR(50) COMMENT '发票号';
ALTER TABLE inventory ADD COLUMN IF NOT EXISTS invoice_date DATETIME COMMENT '发票日期';
ALTER TABLE inventory ADD COLUMN IF NOT EXISTS acceptance_date DATETIME COMMENT '验收日期';
ALTER TABLE inventory ADD COLUMN IF NOT EXISTS status VARCHAR(20) COMMENT '状态';


USE wx;
-- Medicine Table
ALTER TABLE medicine ADD COLUMN IF NOT EXISTS dosage_form VARCHAR(50) COMMENT '剂型';
ALTER TABLE medicine ADD COLUMN IF NOT EXISTS product_code VARCHAR(50) COMMENT '商品货号';
ALTER TABLE medicine ADD COLUMN IF NOT EXISTS manufacturer VARCHAR(100) COMMENT '生产厂家';
ALTER TABLE medicine ADD COLUMN IF NOT EXISTS usage_dosage VARCHAR(255) COMMENT '用法用量';
ALTER TABLE medicine ADD COLUMN IF NOT EXISTS contraindication VARCHAR(255) COMMENT '禁忌症';
ALTER TABLE medicine ADD COLUMN IF NOT EXISTS barcode VARCHAR(64) COMMENT '条码';

-- Inventory Table
ALTER TABLE inventory ADD COLUMN IF NOT EXISTS warehouse VARCHAR(50) COMMENT '仓库';
ALTER TABLE inventory ADD COLUMN IF NOT EXISTS inbound_date DATETIME COMMENT '入库日期';
ALTER TABLE inventory ADD COLUMN IF NOT EXISTS acceptance_no VARCHAR(50) COMMENT '验收单号';
ALTER TABLE inventory ADD COLUMN IF NOT EXISTS invoice_no VARCHAR(50) COMMENT '发票号';
ALTER TABLE inventory ADD COLUMN IF NOT EXISTS invoice_date DATETIME COMMENT '发票日期';
ALTER TABLE inventory ADD COLUMN IF NOT EXISTS acceptance_date DATETIME COMMENT '验收日期';
ALTER TABLE inventory ADD COLUMN IF NOT EXISTS status VARCHAR(20) COMMENT '状态';


USE rzt_db;
-- Medicine Table
ALTER TABLE medicine ADD COLUMN IF NOT EXISTS dosage_form VARCHAR(50) COMMENT '剂型';
ALTER TABLE medicine ADD COLUMN IF NOT EXISTS product_code VARCHAR(50) COMMENT '商品货号';
ALTER TABLE medicine ADD COLUMN IF NOT EXISTS manufacturer VARCHAR(100) COMMENT '生产厂家';
ALTER TABLE medicine ADD COLUMN IF NOT EXISTS usage_dosage VARCHAR(255) COMMENT '用法用量';
ALTER TABLE medicine ADD COLUMN IF NOT EXISTS contraindication VARCHAR(255) COMMENT '禁忌症';
ALTER TABLE medicine ADD COLUMN IF NOT EXISTS barcode VARCHAR(64) COMMENT '条码';

-- Inventory Table
ALTER TABLE inventory ADD COLUMN IF NOT EXISTS warehouse VARCHAR(50) COMMENT '仓库';
ALTER TABLE inventory ADD COLUMN IF NOT EXISTS inbound_date DATETIME COMMENT '入库日期';
ALTER TABLE inventory ADD COLUMN IF NOT EXISTS acceptance_no VARCHAR(50) COMMENT '验收单号';
ALTER TABLE inventory ADD COLUMN IF NOT EXISTS invoice_no VARCHAR(50) COMMENT '发票号';
ALTER TABLE inventory ADD COLUMN IF NOT EXISTS invoice_date DATETIME COMMENT '发票日期';
ALTER TABLE inventory ADD COLUMN IF NOT EXISTS acceptance_date DATETIME COMMENT '验收日期';
ALTER TABLE inventory ADD COLUMN IF NOT EXISTS status VARCHAR(20) COMMENT '状态';

