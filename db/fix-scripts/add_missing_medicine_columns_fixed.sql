-- Fix missing columns in medicine table (Compatible with MySQL < 8.0.29)
USE bht;
ALTER TABLE medicine ADD COLUMN dosage_form VARCHAR(50);
ALTER TABLE medicine ADD COLUMN product_code VARCHAR(50);
ALTER TABLE medicine ADD COLUMN usage_dosage VARCHAR(255);
ALTER TABLE medicine ADD COLUMN contraindication VARCHAR(255);

USE wx;
ALTER TABLE medicine ADD COLUMN dosage_form VARCHAR(50);
ALTER TABLE medicine ADD COLUMN product_code VARCHAR(50);
ALTER TABLE medicine ADD COLUMN usage_dosage VARCHAR(255);
ALTER TABLE medicine ADD COLUMN contraindication VARCHAR(255);

USE rzt_db;
ALTER TABLE medicine ADD COLUMN dosage_form VARCHAR(50);
ALTER TABLE medicine ADD COLUMN product_code VARCHAR(50);
ALTER TABLE medicine ADD COLUMN usage_dosage VARCHAR(255);
ALTER TABLE medicine ADD COLUMN contraindication VARCHAR(255);

