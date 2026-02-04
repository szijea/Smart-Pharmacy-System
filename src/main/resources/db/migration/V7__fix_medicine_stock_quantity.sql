-- 迁移：确保常用测试药品有足够库存
-- 修复布洛芬缓释胶囊库存

-- 更新布洛芬（MSEED005）库存为100以上
UPDATE inventory 
SET stock_quantity = 100 
WHERE medicine_id = 'MSEED005' AND batch_no LIKE '%BLF%';

-- 如果有多个批次，确保至少一个有充足库存
INSERT IGNORE INTO inventory (
  medicine_id, batch_no, expire_date, stock_quantity, min_threshold, purchase_price
) VALUES
('MSEED005','BATCH-BLF-202504','2025-12-10',100,20,7.50);

-- 其他常用药品也补齐库存
UPDATE inventory 
SET stock_quantity = 100 
WHERE medicine_id IN ('MSEED001','MSEED002','MSEED004') 
AND stock_quantity < 50;

INSERT IGNORE INTO inventory (
  medicine_id, batch_no, expire_date, stock_quantity, min_threshold, purchase_price
) VALUES
('MSEED003','BATCH-GML-202504','2025-09-01',50,15,18.00);
