-- ============================================================
-- hotel_room — room-service 数据库
-- 房型表 / 房间表 / 打扫任务表 / 维修记录表
-- ============================================================

DROP DATABASE IF EXISTS hotel_room;
CREATE DATABASE hotel_room DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE hotel_room;

-- 1. 房型表
CREATE TABLE room_type (
    id          BIGINT          NOT NULL AUTO_INCREMENT COMMENT '主键（应用层雪花算法，DB层自增兜底）',
    name        VARCHAR(64)     NOT NULL                 COMMENT '房型名称',
    description VARCHAR(256)    DEFAULT NULL             COMMENT '简要描述',
    area        INT             DEFAULT NULL             COMMENT '面积（㎡）',
    bed_type    VARCHAR(32)     DEFAULT NULL             COMMENT '床型（大床/双床/套房）',
    max_guests  INT             NOT NULL DEFAULT 2       COMMENT '最大入住人数',
    price       DECIMAL(10,2)   NOT NULL                 COMMENT '标准价格（元/晚）',
    cover_image VARCHAR(256)    DEFAULT NULL             COMMENT '封面图 URL',
    images      TEXT            DEFAULT NULL             COMMENT '图片列表（JSON 数组）',
    amenities   VARCHAR(256)    DEFAULT NULL             COMMENT '设施（逗号分隔）',
    sort_order  INT             NOT NULL DEFAULT 0       COMMENT '排序权重',
    is_deleted  TINYINT         NOT NULL DEFAULT 0       COMMENT '逻辑删除',
    create_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id)
) ENGINE=InnoDB COMMENT='房型表';

-- 2. 房间表
CREATE TABLE room (
    id            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键（应用层雪花算法，DB层自增兜底）',
    room_number   VARCHAR(16)   NOT NULL                 COMMENT '房间编号',
    room_type_id  BIGINT        NOT NULL                 COMMENT '房型 ID',
    floor         INT           DEFAULT NULL             COMMENT '楼层',
    status        VARCHAR(16)   NOT NULL DEFAULT '空闲中' COMMENT '维修中/空闲中/预订中/入住中/打扫中/待清洁中',
    price         DECIMAL(10,2) DEFAULT NULL             COMMENT '价格（元/晚，为空时继承房型标准价）',
    description   VARCHAR(128)  DEFAULT NULL             COMMENT '备注',
    version       INT           NOT NULL DEFAULT 0       COMMENT '乐观锁版本号',
    is_deleted    TINYINT       NOT NULL DEFAULT 0       COMMENT '逻辑删除',
    create_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_room_number (room_number),
    KEY idx_status (status),
    KEY idx_room_type_id (room_type_id)
) ENGINE=InnoDB COMMENT='房间表';

-- 3. 打扫任务表
CREATE TABLE cleaning_task (
    id            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键（应用层雪花算法，DB层自增兜底）',
    room_id       BIGINT        NOT NULL COMMENT '房间 ID',
    room_number   VARCHAR(16)   NOT NULL                 COMMENT '房间号（冗余）',
    cleaner_id    BIGINT        NOT NULL                 COMMENT '保洁员 ID',
    cleaner_name  VARCHAR(32)   NOT NULL                 COMMENT '保洁员姓名（冗余）',
    status        VARCHAR(16)   NOT NULL DEFAULT '打扫中' COMMENT '打扫中/已完成',
    start_time    DATETIME      NOT NULL                 COMMENT '开始打扫时间',
    end_time      DATETIME      DEFAULT NULL             COMMENT '打扫完成时间',
    create_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_room_id (room_id),
    KEY idx_cleaner_id (cleaner_id),
    KEY idx_status (status),
    KEY idx_start_time (start_time)
) ENGINE=InnoDB COMMENT='打扫任务表';

-- 4. 维修记录表
CREATE TABLE maintenance_record (
    id            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键（应用层雪花算法，DB层自增兜底）',
    room_id       BIGINT        NOT NULL COMMENT '房间 ID',
    room_number   VARCHAR(16)   NOT NULL                 COMMENT '房间号（冗余）',
    reason        VARCHAR(256)  NOT NULL                 COMMENT '维修原因',
    status        VARCHAR(16)   NOT NULL DEFAULT '维修中' COMMENT '维修中/已完成',
    start_time    DATETIME      NOT NULL                 COMMENT '开始时间',
    end_time      DATETIME      DEFAULT NULL             COMMENT '完成时间',
    cost          DECIMAL(10,2) NOT NULL DEFAULT 0.00    COMMENT '维修费用',
    create_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_room_id (room_id)
) ENGINE=InnoDB COMMENT='维修记录表';

-- ======================== 初始化数据 ========================

INSERT INTO room_type (id, name, description, area, bed_type, max_guests, price, amenities, sort_order) VALUES
(1, '豪华大床房', '35㎡，独立卫浴，城市景观',    35, '大床', 2, 588.00, 'WiFi,空调,浴缸,迷你吧',       1),
(2, '商务双床房', '40㎡，独立卫浴，办公桌',      40, '双床', 2, 488.00, 'WiFi,空调,办公桌',             2),
(3, '行政套房',   '60㎡，独立客厅+卧室，江景',   60, '大床', 2, 988.00, 'WiFi,空调,浴缸,客厅,智能音箱', 3),
(4, '经济单人间', '20㎡，独立卫浴',              20, '单床', 1, 288.00, 'WiFi,空调',                     4);

INSERT INTO room (room_number, room_type_id, floor, status, price) VALUES
-- 3F 豪华大床房 (5间)
('301', 1, 3, '空闲中', 588.00),
('302', 1, 3, '空闲中', 588.00),
('303', 1, 3, '空闲中', 588.00),
('304', 1, 3, '空闲中', 588.00),
('305', 1, 3, '空闲中', 588.00),
-- 4F 商务双床房 (5间)
('401', 2, 4, '空闲中', 488.00),
('402', 2, 4, '空闲中', 488.00),
('403', 2, 4, '空闲中', 488.00),
('404', 2, 4, '空闲中', 488.00),
('405', 2, 4, '空闲中', 488.00),
-- 5F 行政套房 (3间)
('501', 3, 5, '空闲中', 988.00),
('502', 3, 5, '空闲中', 988.00),
('503', 3, 5, '空闲中', 988.00),
-- 6F 经济单人间 (6间)
('601', 4, 6, '空闲中', 288.00),
('602', 4, 6, '空闲中', 288.00),
('603', 4, 6, '空闲中', 288.00),
('604', 4, 6, '空闲中', 288.00),
('605', 4, 6, '空闲中', 288.00),
('606', 4, 6, '空闲中', 288.00);
