-- ============================================================
-- hotel_order — order-service 数据库
-- 订单表 / 消费明细表 / 支付记录表
-- ============================================================

DROP DATABASE IF EXISTS hotel_order;
CREATE DATABASE hotel_order DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE hotel_order;

-- 1. 订单表
CREATE TABLE orders (
    id                BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '主键',
    order_no          VARCHAR(32)   NOT NULL                 COMMENT '订单编号',
    customer_id       BIGINT        NOT NULL                 COMMENT '会员 ID',
    customer_name     VARCHAR(32)   NOT NULL                 COMMENT '会员姓名（冗余）',
    customer_phone    VARCHAR(16)   NOT NULL                 COMMENT '会员手机（冗余）',
    room_id           BIGINT        NOT NULL                 COMMENT '房间 ID',
    room_number       VARCHAR(16)   NOT NULL                 COMMENT '房间号（冗余）',
    room_type_name    VARCHAR(64)   NOT NULL                 COMMENT '房型名称（冗余）',
    check_in_date     DATE          NOT NULL                 COMMENT '预计入住日期',
    check_out_date    DATE          NOT NULL                 COMMENT '预计退房日期',
    actual_check_in   DATETIME      DEFAULT NULL             COMMENT '实际入住时间',
    actual_check_out  DATETIME      DEFAULT NULL             COMMENT '实际退房时间',
    nights            INT           NOT NULL                 COMMENT '入住天数',
    room_price        DECIMAL(10,2) NOT NULL                 COMMENT '入住时房价（元/晚）',
    room_total        DECIMAL(10,2) NOT NULL                 COMMENT '房费小计',
    extra_total       DECIMAL(10,2) NOT NULL DEFAULT 0.00    COMMENT '其他消费合计',
    total_amount      DECIMAL(10,2) NOT NULL                 COMMENT '订单总金额',
    paid_amount       DECIMAL(10,2) NOT NULL DEFAULT 0.00    COMMENT '已付金额',
    deposit           DECIMAL(10,2) NOT NULL DEFAULT 0.00    COMMENT '押金',
    status            VARCHAR(16)   NOT NULL DEFAULT '待支付' COMMENT '待支付/已支付/已入住/已完成/已取消/已退款',
    source            VARCHAR(16)   NOT NULL DEFAULT 'ONLINE' COMMENT 'ONLINE/WALK_IN/FRONT_DESK',
    remark            VARCHAR(256)  DEFAULT NULL             COMMENT '备注',
    create_time       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_no (order_no),
    KEY idx_customer_id (customer_id),
    KEY idx_room_id (room_id),
    KEY idx_status (status),
    KEY idx_check_in_date (check_in_date)
) ENGINE=InnoDB COMMENT='订单表';

-- 2. 消费明细表
CREATE TABLE order_extra (
    id            BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '主键',
    order_id      BIGINT        NOT NULL                 COMMENT '订单 ID',
    item_name     VARCHAR(64)   NOT NULL                 COMMENT '消费项目',
    amount        DECIMAL(10,2) NOT NULL                 COMMENT '单价',
    quantity      INT           NOT NULL DEFAULT 1       COMMENT '数量',
    operator_id   BIGINT        DEFAULT NULL             COMMENT '操作人 ID（前台）',
    create_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_order_id (order_id)
) ENGINE=InnoDB COMMENT='消费明细表';

-- 3. 支付记录表
CREATE TABLE payment (
    id            BIGINT        NOT NULL AUTO_INCREMENT  COMMENT '主键',
    payment_no    VARCHAR(32)   NOT NULL                 COMMENT '支付流水号',
    order_id      BIGINT        NOT NULL                 COMMENT '订单 ID',
    order_no      VARCHAR(32)   NOT NULL                 COMMENT '订单编号（冗余）',
    amount        DECIMAL(10,2) NOT NULL                 COMMENT '支付金额',
    method        VARCHAR(16)   NOT NULL                 COMMENT 'CASH/ALIPAY/WECHAT/CARD',
    status        VARCHAR(16)   NOT NULL DEFAULT '成功'  COMMENT '成功/失败/已退款',
    paid_at       DATETIME      DEFAULT NULL             COMMENT '支付时间',
    create_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_payment_no (payment_no),
    KEY idx_order_id (order_id),
    KEY idx_paid_at (paid_at)
) ENGINE=InnoDB COMMENT='支付记录表';
