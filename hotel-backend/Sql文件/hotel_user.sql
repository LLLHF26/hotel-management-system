-- ============================================================
-- hotel_user — user-service 数据库
-- 员工表 / 会员表 / 积分记录表
-- ============================================================

DROP DATABASE IF EXISTS hotel_user;
CREATE DATABASE hotel_user DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE hotel_user;

-- 1. 员工表
CREATE TABLE `user` (
    id          BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    username    VARCHAR(32)     NOT NULL                 COMMENT '登录账号',
    password    VARCHAR(128)    NOT NULL                 COMMENT 'BCrypt 加密密码',
    real_name   VARCHAR(32)     NOT NULL                 COMMENT '真实姓名',
    phone       VARCHAR(16)     DEFAULT NULL             COMMENT '手机号',
    email       VARCHAR(64)     DEFAULT NULL             COMMENT '邮箱',
    role        VARCHAR(16)     NOT NULL DEFAULT 'FRONT_DESK' COMMENT 'ADMIN/FRONT_DESK/CLEANER',
    avatar      VARCHAR(256)    DEFAULT NULL             COMMENT '头像 URL',
    status      TINYINT         NOT NULL DEFAULT 1       COMMENT '1=正常 0=禁用',
    is_deleted  TINYINT         NOT NULL DEFAULT 0       COMMENT '逻辑删除',
    create_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_username (username),
    KEY idx_role (role),
    KEY idx_phone (phone)
) ENGINE=InnoDB COMMENT='员工表';

-- 2. 会员表
CREATE TABLE customer (
    id              BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    real_name       VARCHAR(32)     NOT NULL                 COMMENT '真实姓名',
    id_card         VARCHAR(18)     DEFAULT NULL             COMMENT '身份证号',
    phone           VARCHAR(16)     NOT NULL                 COMMENT '手机号（登录凭证）',
    password        VARCHAR(128)    DEFAULT NULL             COMMENT '密码（可空）',
    gender          TINYINT         DEFAULT NULL             COMMENT '0=男 1=女',
    avatar          VARCHAR(256)    DEFAULT NULL             COMMENT '头像 URL（阿里云 OSS）',
    points          INT             NOT NULL DEFAULT 0       COMMENT '积分余额',
    total_consumed  DECIMAL(10,2)   NOT NULL DEFAULT 0.00    COMMENT '累计消费金额',
    member_level    VARCHAR(16)     NOT NULL DEFAULT 'NORMAL' COMMENT 'NORMAL/SILVER/GOLD/DIAMOND',
    status          TINYINT         NOT NULL DEFAULT 1       COMMENT '1=正常 0=冻结',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_phone (phone),
    KEY idx_id_card (id_card),
    KEY idx_member_level (member_level)
) ENGINE=InnoDB COMMENT='会员表';

-- 3. 积分记录表
CREATE TABLE points_log (
    id              BIGINT          NOT NULL AUTO_INCREMENT  COMMENT '主键',
    customer_id     BIGINT          NOT NULL                 COMMENT '会员 ID',
    type            VARCHAR(16)     NOT NULL                 COMMENT 'EARN=获得 CONSUME=消费',
    points          INT             NOT NULL                 COMMENT '变动数值（正=获得 负=消费）',
    balance_after   INT             NOT NULL                 COMMENT '变动后余额',
    reason          VARCHAR(128)    DEFAULT NULL             COMMENT '变动原因',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_customer_id (customer_id),
    KEY idx_create_time (create_time)
) ENGINE=InnoDB COMMENT='积分记录表';

-- ======================== 初始化数据 ========================

-- 默认员工（密码均为 123456 的 BCrypt 哈希）
INSERT INTO `user` (username, password, real_name, phone, role) VALUES
('admin',     '$2b$10$WdIGIL8bMmA1CzTvoT7qt.1r7HONl8tGBwAaY.MjC3vRPTByS.bYS', '系统管理员', '13800000001', 'ADMIN'),
('front01',   '$2b$10$4cjQkizufQu61X2j5y91BeqsJZUHie4CkmM6zcTVS8ou4C1CCS9wm', '前台小王',   '13800000002', 'FRONT_DESK'),
('cleaner01', '$2b$10$eAb/UhEmqMAnkXUDB77H2u6hIpiZPobHIqTdzVU1kgwNx4P.ix4Vm', '保洁张阿姨', '13800000003', 'CLEANER'),
('cleaner02', '$2b$10$JG34cP.v6mBtDxIyUoUPGuYHNwJCR/eQNRpX0pmggeI9yMSjthibW', '保洁李阿姨', '13800000004', 'CLEANER');
