-- 多租户初始化（扩展版）
-- 注意：仅在第一次启动容器时执行。存在数据请谨慎修改。

CREATE DATABASE IF NOT EXISTS bht CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS wx CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE IF NOT EXISTS rzt_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 公共表结构定义（为避免 LIKE 丢列，显式定义）
-- medicine 完整列
-- inventory 采用统一列: inventory_id, medicine_id, batch_no, create_time, expiry_date, stock_quantity, min_stock, max_stock, purchase_price, supplier, update_time
-- 新增 supplier, order, order_item, stock_in, stock_in_item

DELIMITER $$
CREATE PROCEDURE init_tenant(IN dbname VARCHAR(64))
BEGIN
  SET @sql = CONCAT('USE ', dbname);
  PREPARE stmt FROM @sql; EXECUTE stmt; DEALLOCATE PREPARE stmt;

  CREATE TABLE IF NOT EXISTS category (
    category_id INT PRIMARY KEY AUTO_INCREMENT,
    category_name VARCHAR(50) NOT NULL,
    parent_id INT DEFAULT 0,
    sort INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_name(category_name)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

  CREATE TABLE IF NOT EXISTS role (
    role_id INT PRIMARY KEY AUTO_INCREMENT,
    role_name VARCHAR(50) NOT NULL,
    permissions TEXT,
    UNIQUE KEY uk_role_name(role_name)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

  CREATE TABLE IF NOT EXISTS member (
    member_id VARCHAR(32) PRIMARY KEY,
    name VARCHAR(50) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    level TINYINT DEFAULT 0,
    points INT DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_phone(phone)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

  CREATE TABLE IF NOT EXISTS employee (
    employee_id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL,
    password VARCHAR(100) NOT NULL,
    name VARCHAR(50) NOT NULL,
    role_id INT NOT NULL,
    phone VARCHAR(20),
    status TINYINT DEFAULT 1,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_username(username),
    KEY idx_role(role_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

  CREATE TABLE IF NOT EXISTS supplier (
    supplier_id INT PRIMARY KEY AUTO_INCREMENT,
    supplier_name VARCHAR(100) NOT NULL,
    contact_person VARCHAR(50),
    phone VARCHAR(30),
    address VARCHAR(200),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

  CREATE TABLE IF NOT EXISTS medicine (
    medicine_id VARCHAR(32) PRIMARY KEY,
    generic_name VARCHAR(100) NOT NULL,
    trade_name VARCHAR(100),
    spec VARCHAR(50) NOT NULL,
    approval_no VARCHAR(50) NOT NULL,
    category_id INT NOT NULL,
    manufacturer VARCHAR(100),
    barcode VARCHAR(64),
    retail_price DECIMAL(10,2) NOT NULL,
    member_price DECIMAL(10,2),
    production_date DATE,
    expiry_date DATE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    deleted TINYINT(1) NOT NULL DEFAULT 0,
    is_rx TINYINT(1) NOT NULL DEFAULT 0,
    unit VARCHAR(20),
    description TEXT,
    supplier_id INT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_category(category_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

  CREATE TABLE IF NOT EXISTS inventory (
    inventory_id INT PRIMARY KEY AUTO_INCREMENT,
    medicine_id VARCHAR(32) NOT NULL,
    batch_no VARCHAR(50),
    create_time DATETIME,
    expiry_date DATE,
    stock_quantity INT NOT NULL DEFAULT 0,
    min_stock INT DEFAULT 10,
    max_stock INT,
    purchase_price DECIMAL(10,2),
    supplier VARCHAR(100),
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_med(medicine_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

  CREATE TABLE IF NOT EXISTS `order` (
    order_id VARCHAR(32) PRIMARY KEY,
    customer_name VARCHAR(100),
    member_id VARCHAR(32),
    cashier_id INT,
    total_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
    discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0,
    actual_payment DECIMAL(10,2) NOT NULL DEFAULT 0,
    payment_type TINYINT DEFAULT 1,
    payment_status TINYINT DEFAULT 1,
    order_time DATETIME,
    pay_time DATETIME,
    refund_time DATETIME,
    refund_reason VARCHAR(200),
    used_points INT,
    created_points INT,
    remark VARCHAR(200),
    KEY idx_member(member_id),
    KEY idx_cashier(cashier_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

  CREATE TABLE IF NOT EXISTS order_item (
    id INT PRIMARY KEY AUTO_INCREMENT,
    order_id VARCHAR(32) NOT NULL,
    medicine_id VARCHAR(32) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    KEY idx_order(order_id),
    KEY idx_med(medicine_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

  CREATE TABLE IF NOT EXISTS stock_in (
    stock_in_id INT PRIMARY KEY AUTO_INCREMENT,
    stock_in_no VARCHAR(40) NOT NULL,
    supplier_id INT,
    operator_id INT,
    stock_in_date DATETIME,
    status TINYINT DEFAULT 1,
    remark VARCHAR(200),
    total_amount DECIMAL(12,2) DEFAULT 0,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    KEY idx_supplier(supplier_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

  CREATE TABLE IF NOT EXISTS stock_in_item (
    item_id INT PRIMARY KEY AUTO_INCREMENT,
    stock_in_id INT NOT NULL,
    medicine_id VARCHAR(32) NOT NULL,
    batch_number VARCHAR(50),
    production_date DATE,
    expiry_date DATE,
    quantity INT NOT NULL,
    unit_price DECIMAL(10,2) NOT NULL,
    KEY idx_stockin(stock_in_id),
    KEY idx_medicine(medicine_id)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

  -- 初始数据（仅当表为空时插入）
  INSERT INTO role(role_name,permissions) SELECT '管理员','["cashier","inventory","member","order","analysis","system"]' WHERE NOT EXISTS(SELECT 1 FROM role WHERE role_name='管理员');
  INSERT INTO role(role_name,permissions) SELECT '店长','["cashier","order","member"]' WHERE NOT EXISTS(SELECT 1 FROM role WHERE role_name='店长');
  INSERT INTO role(role_name,permissions) SELECT '店员','["cashier"]' WHERE NOT EXISTS(SELECT 1 FROM role WHERE role_name='店员');

  INSERT INTO employee(username,password,name,role_id,phone) SELECT 'admin'||dbname,'e10adc3949ba59abbe56e057f20f883e','管理员',1,'13800138000' WHERE NOT EXISTS(SELECT 1 FROM employee WHERE username='admin'||dbname);
  INSERT INTO employee(username,password,name,role_id,phone) SELECT dbname||'01','e10adc3949ba59abbe56e057f20f883e','店长',2,'13800138001' WHERE NOT EXISTS(SELECT 1 FROM employee WHERE username=dbname||'01');

  INSERT INTO member(member_id,name,phone,level,points) SELECT 'M00001','李四','13700137000',1,500 WHERE NOT EXISTS(SELECT 1 FROM member WHERE member_id='M00001');
  INSERT INTO member(member_id,name,phone,level,points) SELECT 'M00002','王五','13600136000',0,120 WHERE NOT EXISTS(SELECT 1 FROM member WHERE member_id='M00002');

  INSERT INTO supplier(supplier_name,contact_person,phone,address) SELECT '默认供应商','系统','13800000000','系统自动创建' WHERE NOT EXISTS(SELECT 1 FROM supplier WHERE supplier_name='默认供应商');
END$$
DELIMITER ;

CALL init_tenant('bht');
CALL init_tenant('wx');
CALL init_tenant('rzt_db');

-- 补充 boss 表 (平台管理员)
USE rzt_db;
CREATE TABLE IF NOT EXISTS boss (
  id INT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) NOT NULL UNIQUE,
  password VARCHAR(100) NOT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
-- 默认管理员 admin / 123456
INSERT INTO boss(username, password) SELECT 'admin', 'e10adc3949ba59abbe56e057f20f883e' WHERE NOT EXISTS(SELECT 1 FROM boss WHERE username='admin');

-- 清理过程
DROP PROCEDURE IF EXISTS init_tenant;
