-- V3 初始化各租户的角色与员工账号（幂等）
-- 注意：该脚本会在所有租户数据库执行（default, bht, wx, rzt）
-- 如果某租户已存在相同用户名或角色名，INSERT IGNORE 将跳过，保证安全。

-- 1. 初始化角色（若不存在）
INSERT IGNORE INTO role(role_name, permissions) VALUES
 ('管理员', '["cashier","inventory","member","order","analysis","system"]'),
 ('收银员', '["cashier","order","member"]'),
 ('库管员', '["inventory","stock_record"]');

-- 2. 初始化 bht 租户员工（在所有库执行，用户名仅本库唯一即可）
-- 若你希望仅在 bht 库创建，可以手动在 bht 数据库执行；这里统一执行，其他库中也会出现这些账号（可按需删除）。
INSERT IGNORE INTO employee(username, password, name, role_id, phone, status)
VALUES
 ('adminbht', MD5('123456'), '管理员', (SELECT role_id FROM role WHERE role_name='管理员'), '13800138000', 1),
 ('bht01',    MD5('123456'), '店长',   (SELECT role_id FROM role WHERE role_name='收银员'), '13800138001', 1),
 ('bht02',    MD5('123456'), '员工',   (SELECT role_id FROM role WHERE role_name='库管员'), '13800138002', 1);

-- 3. 初始化 rzt 租户员工账号（同理会在所有库执行，若仅需 rzt 库请手动运行在 rzt_db）
INSERT IGNORE INTO employee(username, password, name, role_id, phone, status)
VALUES
 ('adminrzt', MD5('123456'), '系统管理员', (SELECT role_id FROM role WHERE role_name='管理员'), '13800138010', 1),
 ('rzt01',    MD5('123456'), '店长',       (SELECT role_id FROM role WHERE role_name='收银员'), '13800138011', 1),
 ('rzt02',    MD5('123456'), '店员',       (SELECT role_id FROM role WHERE role_name='库管员'), '13800138012', 1);

-- 4. 初始化 wx 租户员工账号
INSERT IGNORE INTO employee(username, password, name, role_id, phone, status)
VALUES
 ('adminwx', MD5('123456'), '管理员', (SELECT role_id FROM role WHERE role_name='管理员'), '13800138020', 1),
 ('wx01',    MD5('123456'), '店长',   (SELECT role_id FROM role WHERE role_name='收银员'), '13800138021', 1),
 ('wx02',    MD5('123456'), '店员',   (SELECT role_id FROM role WHERE role_name='库管员'), '13800138022', 1);

-- 如果你希望不同租户有不同的账号集：
-- 可分拆为 V3_bht__init.sql / V3_rzt__init.sql / V3_wx__init.sql 并手动在对应库执行。
-- 当前方案简单统一，便于快速验证登录。

-- 5. 验证示例（在 MySQL 中手动执行，不在迁移脚本内）
-- SELECT username, role_id FROM employee ORDER BY username;

