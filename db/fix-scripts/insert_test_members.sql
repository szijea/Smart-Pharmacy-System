-- Insert test members for verification
INSERT INTO `member` (member_id, name, phone, level, points, balance, consumption_count, create_time, status)
VALUES
('M001', '张三', '13800138000', 1, 500, 100.00, 5, NOW(), 'ACTIVE'),
('M002', '李四', '13900139000', 2, 1200, 500.00, 12, NOW(), 'ACTIVE'),
('M003', '王五', '13700137000', 4, 5000, 2000.00, 50, NOW(), 'ACTIVE')
ON DUPLICATE KEY UPDATE
points = VALUES(points),
level = VALUES(level);

