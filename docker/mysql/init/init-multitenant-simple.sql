-- 简化版初始化：不使用存储过程，直接三套结构

-- bht
CREATE DATABASE IF NOT EXISTS bht CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE bht;
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
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
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
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
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
  remark VARCHAR(200)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS order_item (
  id INT PRIMARY KEY AUTO_INCREMENT,
  order_id VARCHAR(32) NOT NULL,
  medicine_id VARCHAR(32) NOT NULL,
  quantity INT NOT NULL,
  unit_price DECIMAL(10,2) NOT NULL,
  subtotal DECIMAL(10,2) NOT NULL
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
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE TABLE IF NOT EXISTS stock_in_item (
  id INT PRIMARY KEY AUTO_INCREMENT,
  stock_in_id INT NOT NULL,
  medicine_id VARCHAR(32) NOT NULL,
  batch_number VARCHAR(50),
  production_date DATE,
  expiry_date DATE,
  quantity INT NOT NULL,
  unit_price DECIMAL(10,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
INSERT INTO supplier(supplier_name,contact_person,phone,address) SELECT '默认供应商','系统','13800000000','系统自动创建' WHERE NOT EXISTS(SELECT 1 FROM supplier WHERE supplier_name='默认供应商');

-- wx
CREATE DATABASE IF NOT EXISTS wx CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE wx;
CREATE TABLE IF NOT EXISTS supplier LIKE bht.supplier;
CREATE TABLE IF NOT EXISTS medicine LIKE bht.medicine;
CREATE TABLE IF NOT EXISTS inventory LIKE bht.inventory;
CREATE TABLE IF NOT EXISTS `order` LIKE bht.`order`;
CREATE TABLE IF NOT EXISTS order_item LIKE bht.order_item;
CREATE TABLE IF NOT EXISTS stock_in LIKE bht.stock_in;
CREATE TABLE IF NOT EXISTS stock_in_item LIKE bht.stock_in_item;
INSERT INTO supplier(supplier_name,contact_person,phone,address) SELECT '默认供应商','系统','13800000000','系统自动创建' WHERE NOT EXISTS(SELECT 1 FROM supplier WHERE supplier_name='默认供应商');

-- rzt_db
CREATE DATABASE IF NOT EXISTS rzt_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE rzt_db;
CREATE TABLE IF NOT EXISTS supplier LIKE bht.supplier;
CREATE TABLE IF NOT EXISTS medicine LIKE bht.medicine;
CREATE TABLE IF NOT EXISTS inventory LIKE bht.inventory;
CREATE TABLE IF NOT EXISTS `order` LIKE bht.`order`;
CREATE TABLE IF NOT EXISTS order_item LIKE bht.order_item;
CREATE TABLE IF NOT EXISTS stock_in LIKE bht.stock_in;
CREATE TABLE IF NOT EXISTS stock_in_item LIKE bht.stock_in_item;
INSERT INTO supplier(supplier_name,contact_person,phone,address) SELECT '默认供应商','系统','13800000000','系统自动创建' WHERE NOT EXISTS(SELECT 1 FROM supplier WHERE supplier_name='默认供应商');

