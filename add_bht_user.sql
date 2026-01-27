-- 添加 bht 用户到所有租户数据库
-- 用户名: bht, 密码: 123456 (MD5: e10adc3949ba59abbe56e057f20f883e)

USE bht;
INSERT INTO employee (username, password, name, role_id, phone, status)
SELECT 'bht', 'e10adc3949ba59abbe56e057f20f883e', 'BHT管理员', role_id, '13800138000', 1
FROM role WHERE role_name='管理员'
ON DUPLICATE KEY UPDATE password='e10adc3949ba59abbe56e057f20f883e';

USE wx;
INSERT INTO employee (username, password, name, role_id, phone, status)
SELECT 'bht', 'e10adc3949ba59abbe56e057f20f883e', 'WX管理员', role_id, '13800138000', 1
FROM role WHERE role_name='管理员'
ON DUPLICATE KEY UPDATE password='e10adc3949ba59abbe56e057f20f883e';

USE rzt_db;
INSERT INTO employee (username, password, name, role_id, phone, status)
SELECT 'bht', 'e10adc3949ba59abbe56e057f20f883e', 'RZT管理员', role_id, '13800138000', 1
FROM role WHERE role_name='管理员'
ON DUPLICATE KEY UPDATE password='e10adc3949ba59abbe56e057f20f883e';

-- 验证
SELECT '=== BHT数据库 ===' as info;
USE bht;
SELECT employee_id, username, name, status FROM employee WHERE username='bht';

SELECT '=== WX数据库 ===' as info;
USE wx;
SELECT employee_id, username, name, status FROM employee WHERE username='bht';

SELECT '=== RZT数据库 ===' as info;
USE rzt_db;
SELECT employee_id, username, name, status FROM employee WHERE username='bht';

