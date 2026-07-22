-- ============================================================
-- hotel_system — system-service 数据库
-- 系统设置表（酒店级配置：名称/电话/地址/入住退房时间/满房预警阈值等）
-- ============================================================

DROP DATABASE IF EXISTS hotel_system;
CREATE DATABASE hotel_system DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE hotel_system;

-- 1. 系统设置表（key-value 形式，跨业务域共享）
CREATE TABLE system_setting (
    `key`         VARCHAR(64)     NOT NULL                 COMMENT '配置键（主键）',
    value         TEXT            NOT NULL                 COMMENT '配置值',
    description   VARCHAR(128)    NOT NULL DEFAULT ''      COMMENT '描述',
    create_time   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`key`)
) ENGINE=InnoDB COMMENT='系统设置表';

-- 2. 默认数据（首次部署 seed；应用层也会在表为空时自动 seed）
INSERT INTO system_setting (`key`, value, description) VALUES
    ('hotelName',          '云端酒店',                  '酒店名称'),
    ('phone',              '010-88882222',              '联系电话'),
    ('address',            '北京市朝阳区示例路 88 号',    '酒店地址'),
    ('checkInTime',        '14:00',                     '入住时间'),
    ('checkOutTime',       '12:00',                     '退房时间'),
    ('fullHouseThreshold', '90%',                       '满房预警阈值'),
    ('points_discount_ratio',   '0.1',  '1 积分可抵扣的金额（元），用于客房商品下单抵扣'),
    ('points_discount_enabled', 'true', '是否启用积分抵扣（true/false）');
