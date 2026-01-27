-- 1. 创建数据库并指定字符集（若已存在则跳过）
CREATE DATABASE IF NOT EXISTS rzt_db 
  CHARACTER SET utf8mb4 
  COLLATE utf8mb4_unicode_ci;

-- 切换到目标数据库
USE rzt_db;

-- 2. 基础配置表（先删除旧表，再创建新表）
-- 药品分类表
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
                            `category_id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '分类ID',
                            `category_name` VARCHAR(50) NOT NULL COMMENT '分类名称（如：感冒药、消炎药）',
                            `parent_id` INT DEFAULT 0 COMMENT '父分类ID（0表示一级分类）',
                            `sort` INT DEFAULT 0 COMMENT '排序（数字越小越靠前）',
                            `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            UNIQUE KEY `uk_name` (`category_name`) COMMENT '分类名称唯一'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '药品分类表';

-- 角色表（权限管理）
DROP TABLE IF EXISTS `role`;
CREATE TABLE `role` (
                        `role_id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
                        `role_name` VARCHAR(50) NOT NULL COMMENT '角色名称（如：管理员、收银员）',
                        `permissions` TEXT COMMENT '权限列表（JSON格式，如：["cashier","inventory"]）',
                        UNIQUE KEY `uk_role_name` (`role_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '角色表';

-- 会员表
DROP TABLE IF EXISTS `member`;
CREATE TABLE `member` (
                          `member_id` VARCHAR(32) PRIMARY KEY COMMENT '会员ID（如：M001）',
                          `name` VARCHAR(50) NOT NULL COMMENT '姓名',
                          `phone` VARCHAR(20) NOT NULL COMMENT '手机号（登录/查询用）',
                          `card_no` VARCHAR(50) COMMENT '会员卡编号',
                          `level` TINYINT DEFAULT 0 COMMENT '会员等级：0-普通，1-银卡，2-金卡',
                          `points` INT DEFAULT 0 COMMENT '当前积分',
                          `allergic_history` TEXT COMMENT '过敏史',
                          `medical_card_no` VARCHAR(50) COMMENT '医保卡号',
                          `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
                          UNIQUE KEY `uk_phone` (`phone`) COMMENT '手机号唯一',
                          KEY `idx_card` (`card_no`) COMMENT '按卡号查询'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '会员表';

-- 处方表
DROP TABLE IF EXISTS `prescription`;
CREATE TABLE `prescription` (
                                `prescription_id` VARCHAR(32) PRIMARY KEY COMMENT '处方ID（如：P001）',
                                `patient_name` VARCHAR(50) COMMENT '患者姓名',
                                `doctor_name` VARCHAR(50) COMMENT '开方医生',
                                `hospital` VARCHAR(100) COMMENT '医院名称',
                                `prescription_date` DATE COMMENT '开方日期',
                                `img_url` VARCHAR(255) NOT NULL COMMENT '处方图片URL（存储路径）',
                                `verify_status` TINYINT DEFAULT 0 COMMENT '审核状态：0-待审核，1-通过，2-拒绝',
                                `verify_remark` VARCHAR(200) COMMENT '审核备注',
                                `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
                                KEY `idx_patient` (`patient_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '处方表（处方药必备）';

-- 3. 依赖基础表的核心业务表
-- 员工表（依赖role表）
DROP TABLE IF EXISTS `employee`;
CREATE TABLE `employee` (
                            `employee_id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '员工ID',
                            `username` VARCHAR(50) NOT NULL COMMENT '登录账号',
                            `password` VARCHAR(100) NOT NULL COMMENT '密码（加密存储）',
                            `name` VARCHAR(50) NOT NULL COMMENT '姓名',
                            `role_id` INT NOT NULL COMMENT '角色ID（关联role表）',
                            `phone` VARCHAR(20) COMMENT '手机号',
                            `status` TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
                            `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP,
                            UNIQUE KEY `uk_username` (`username`),
                            KEY `idx_role` (`role_id`),
                            CONSTRAINT `fk_employee_role` FOREIGN KEY (`role_id`) REFERENCES `role` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '员工表';

-- 药品信息表（依赖category表）
DROP TABLE IF EXISTS `medicine`;
CREATE TABLE `medicine` (
                            `medicine_id` VARCHAR(32) PRIMARY KEY COMMENT '药品ID（自定义唯一标识，如：M001）',
                            `generic_name` VARCHAR(100) NOT NULL COMMENT '通用名（如：复方氨酚烷胺胶囊）',
                            `trade_name` VARCHAR(100) COMMENT '商品名（如：快克）',
                            `spec` VARCHAR(50) NOT NULL COMMENT '规格（如：12粒/盒）',
                            `approval_no` VARCHAR(50) NOT NULL COMMENT '批准文号（国药准字XXX）',
                            `category_id` INT NOT NULL COMMENT '所属分类ID（关联category表）',
                            `manufacturer` VARCHAR(100) COMMENT '生产厂家',
                            `retail_price` DECIMAL(10,2) NOT NULL COMMENT '零售价',
                            `member_price` DECIMAL(10,2) COMMENT '会员价',
                            `is_rx` TINYINT NOT NULL DEFAULT 0 COMMENT '是否处方药：0-非处方药，1-处方药',
                            `unit` VARCHAR(20) COMMENT '单位（如：盒、瓶）',
                            `description` TEXT COMMENT '药品描述（适应症等）',
                            `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
                            `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
                            KEY `idx_category` (`category_id`) COMMENT '按分类查询索引',
                            KEY `idx_name` (`generic_name`,`trade_name`) COMMENT '按名称搜索索引',
                            KEY `idx_approval` (`approval_no`) COMMENT '按批准文号查询索引',
                            CONSTRAINT `fk_medicine_category` FOREIGN KEY (`category_id`) REFERENCES `category` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '药品信息表';

-- 4. 依赖核心业务表的扩展表
-- 库存表（依赖medicine表）
DROP TABLE IF EXISTS `inventory`;
CREATE TABLE `inventory` (
                             `inventory_id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '库存ID',
                             `medicine_id` VARCHAR(32) NOT NULL COMMENT '药品ID（关联medicine表）',
                             `batch_no` VARCHAR(50) COMMENT '批号',
                             `expire_date` DATE COMMENT '有效期至',
                             `stock_quantity` INT NOT NULL DEFAULT 0 COMMENT '当前库存数量',
                             `min_threshold` INT DEFAULT 10 COMMENT '最低库存阈值（低于此值预警）',
                             `purchase_price` DECIMAL(10,2) COMMENT '采购价（用于计算成本）',
                             `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '库存更新时间',
                             UNIQUE KEY `uk_medicine_batch` (`medicine_id`,`batch_no`) COMMENT '同一药品同一批号唯一',
                             KEY `idx_medicine` (`medicine_id`) COMMENT '按药品查询库存',
                             KEY `idx_expire` (`expire_date`) COMMENT '按效期查询（近效期预警）',
                             CONSTRAINT `fk_inventory_medicine` FOREIGN KEY (`medicine_id`) REFERENCES `medicine` (`medicine_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '药品库存表（按药品+批号管理）';

-- 会员积分记录表（依赖member表）
DROP TABLE IF EXISTS `member_point`;
CREATE TABLE `member_point` (
                                `id` INT PRIMARY KEY AUTO_INCREMENT,
                                `member_id` VARCHAR(32) NOT NULL COMMENT '会员ID',
                                `point` INT NOT NULL COMMENT '积分变动（正数增加，负数减少）',
                                `type` TINYINT NOT NULL COMMENT '类型：1-消费获得，2-积分抵扣，3-积分兑换',
                                `related_order_id` VARCHAR(32) COMMENT '关联订单ID',
                                `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '变动时间',
                                KEY `idx_member` (`member_id`),
                                CONSTRAINT `fk_point_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '会员积分变动记录';

-- 库存变动记录表（依赖medicine、employee表）
DROP TABLE IF EXISTS `stock_record`;
CREATE TABLE `stock_record` (
                                `record_id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
                                `medicine_id` VARCHAR(32) NOT NULL COMMENT '药品ID',
                                `batch_no` VARCHAR(50) COMMENT '批号',
                                `change_type` TINYINT NOT NULL COMMENT '变动类型：1-入库，2-出库（销售），3-盘点调整，4-退货入库',
                                `quantity` INT NOT NULL COMMENT '变动数量（正数为增加，负数为减少）',
                                `operator_id` INT NOT NULL COMMENT '操作人ID（关联employee表）',
                                `related_order_id` VARCHAR(32) COMMENT '关联订单ID（出库时必填）',
                                `remark` VARCHAR(200) COMMENT '备注（如：采购入库、销售出库）',
                                `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '变动时间',
                                KEY `idx_medicine` (`medicine_id`),
                                KEY `idx_time` (`create_time`),
                                CONSTRAINT `fk_stock_medicine` FOREIGN KEY (`medicine_id`) REFERENCES `medicine` (`medicine_id`),
                                CONSTRAINT `fk_stock_employee` FOREIGN KEY (`operator_id`) REFERENCES `employee` (`employee_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '库存变动记录表（用于追踪库存变更）';

-- 挂单表（依赖employee表）
DROP TABLE IF EXISTS `hang_order`;
CREATE TABLE `hang_order` (
                              `hang_id` VARCHAR(32) PRIMARY KEY COMMENT '挂单ID（如：H20240520001）',
                              `cashier_id` INT NOT NULL COMMENT '操作人ID（关联employee表）',
                              `hang_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '挂单时间',
                              `status` TINYINT DEFAULT 0 COMMENT '状态：0-未结算，1-已结算，2-已取消',
                              `remark` VARCHAR(200) COMMENT '备注',
                              KEY `idx_cashier` (`cashier_id`),
                              KEY `idx_time` (`hang_time`),
                              CONSTRAINT `fk_hang_employee` FOREIGN KEY (`cashier_id`) REFERENCES `employee` (`employee_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '挂单表（临时保存未结算订单）';

-- 挂单项表（依赖hang_order表）
DROP TABLE IF EXISTS `hang_order_item`;
CREATE TABLE `hang_order_item` (
                                   `id` INT PRIMARY KEY AUTO_INCREMENT,
                                   `hang_id` VARCHAR(32) NOT NULL COMMENT '挂单ID（关联hang_order表）',
                                   `medicine_id` VARCHAR(32) NOT NULL COMMENT '药品ID',
                                   `quantity` INT NOT NULL COMMENT '数量',
                                   `unit_price` DECIMAL(10,2) NOT NULL COMMENT '单价',
                                   KEY `idx_hang` (`hang_id`),
                                   CONSTRAINT `fk_hang_item` FOREIGN KEY (`hang_id`) REFERENCES `hang_order` (`hang_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '挂单项表';

-- 订单表（依赖employee、member表）
DROP TABLE IF EXISTS `order`;
CREATE TABLE `order` (
                         `order_id` VARCHAR(32) PRIMARY KEY COMMENT '订单ID（如：O20240520001）',
                         `cashier_id` INT NOT NULL COMMENT '收银员ID（关联employee表）',
                         `member_id` VARCHAR(32) COMMENT '会员ID（非会员为NULL，关联member表）',
                         `total_amount` DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
                         `discount_amount` DECIMAL(10,2) DEFAULT 0 COMMENT '优惠金额（会员价/积分抵扣等）',
                         `actual_payment` DECIMAL(10,2) NOT NULL COMMENT '实付金额',
                         `payment_type` TINYINT NOT NULL COMMENT '支付方式：1-现金，2-微信，3-支付宝，4-医保',
                         `payment_status` TINYINT NOT NULL DEFAULT 0 COMMENT '支付状态：0-未支付，1-已支付，2-退款',
                         `used_points` INT DEFAULT 0 COMMENT '使用的积分（抵扣金额）',
                         `created_points` INT DEFAULT 0 COMMENT '本次消费产生的积分',
                         `order_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
                         `pay_time` DATETIME COMMENT '支付时间',
                         `remark` VARCHAR(200) COMMENT '备注（如：挂单后结算）',
                         KEY `idx_cashier` (`cashier_id`),
                         KEY `idx_member` (`member_id`),
                         KEY `idx_time` (`order_time`),
                         CONSTRAINT `fk_order_employee` FOREIGN KEY (`cashier_id`) REFERENCES `employee` (`employee_id`),
                         CONSTRAINT `fk_order_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '订单表';

-- 订单项表（依赖order、medicine、prescription表）
DROP TABLE IF EXISTS `order_item`;
CREATE TABLE `order_item` (
                              `item_id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '订单项ID',
                              `order_id` VARCHAR(32) NOT NULL COMMENT '订单ID（关联order表）',
                              `medicine_id` VARCHAR(32) NOT NULL COMMENT '药品ID（关联medicine表）',
                              `quantity` INT NOT NULL COMMENT '购买数量',
                              `unit_price` DECIMAL(10,2) NOT NULL COMMENT '实际成交单价（零售价/会员价）',
                              `subtotal` DECIMAL(10,2) NOT NULL COMMENT '小计金额（数量×单价）',
                              `prescription_id` VARCHAR(32) COMMENT '处方ID（处方药必填，关联prescription表）',
                              KEY `idx_order` (`order_id`),
                              KEY `idx_medicine` (`medicine_id`),
                              CONSTRAINT `fk_item_order` FOREIGN KEY (`order_id`) REFERENCES `order` (`order_id`),
                              CONSTRAINT `fk_item_medicine` FOREIGN KEY (`medicine_id`) REFERENCES `medicine` (`medicine_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT '订单项表（订单关联的药品明细）';

-- 5. 初始化基础数据
INSERT INTO `category` (`category_name`, `parent_id`, `sort`) VALUES
                                                                  ('感冒药', 0, 1),
                                                                  ('消炎药', 0, 2),
                                                                  ('慢性病药', 0, 3),
                                                                  ('维生素类', 0, 4),
                                                                  ('中药饮片', 0, 5),
                                                                  ('保健品', 0, 6);

INSERT INTO `role` (`role_name`, `permissions`) VALUES
                                                    ('管理员', '["cashier","inventory","member","order","analysis","system"]'),
                                                    ('收银员', '["cashier","order","member"]'),
                                                    ('库管员', '["inventory","stock_record"]');

INSERT INTO `employee` (`username`, `password`, `name`, `role_id`, `phone`) VALUES
                                                                                ('adminrzt', 'e10adc3949ba59abbe56e057f20f883e', '系统管理员', 1, '13800138000'),
                                                                                ('rzt01', 'e10adc3949ba59abbe56e057f20f883e', '店长', 2, '13900139000'),
                                                                                ('rzt02', 'e10adc3949ba59abbe56e057f20f883e', '店员', 3, '13900139001');

INSERT INTO `member` (`member_id`, `name`, `phone`, `level`, `points`) VALUES
                                                                           ('M00001', '李四', '13700137000', 1, 500),
                                                                           ('M00002', '王五', '13600136000', 0, 120);