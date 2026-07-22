-- ============================================================
-- hotel_finance — finance-service 数据库
-- 日结营收表 / 退款记录表
-- ============================================================

DROP DATABASE IF EXISTS hotel_finance;
CREATE DATABASE hotel_finance DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE hotel_finance;

-- 1. 日结营收表
CREATE TABLE daily_revenue (
    id                BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键（应用层雪花算法，DB层自增兜底）',
    date              DATE          NOT NULL                 COMMENT '统计日期',
    room_revenue      DECIMAL(12,2) NOT NULL DEFAULT 0.00    COMMENT '房费收入',
    extra_revenue     DECIMAL(12,2) NOT NULL DEFAULT 0.00    COMMENT '其他消费收入',
    total_revenue     DECIMAL(12,2) NOT NULL DEFAULT 0.00    COMMENT '总收入',
    order_count       INT           NOT NULL DEFAULT 0       COMMENT '订单数',
    check_in_count    INT           NOT NULL DEFAULT 0       COMMENT '当日入住数',
    check_out_count   INT           NOT NULL DEFAULT 0       COMMENT '当日退房数',
    cash_amount       DECIMAL(12,2) NOT NULL DEFAULT 0.00    COMMENT '现金收款',
    alipay_amount     DECIMAL(12,2) NOT NULL DEFAULT 0.00    COMMENT '支付宝收款',
    wechat_amount     DECIMAL(12,2) NOT NULL DEFAULT 0.00    COMMENT '微信收款',
    card_amount       DECIMAL(12,2) NOT NULL DEFAULT 0.00    COMMENT '刷卡收款',
    create_time       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_date (date)
) ENGINE=InnoDB COMMENT='日结营收表';

-- 2. 退款记录表
CREATE TABLE refund_record (
    id              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键（应用层雪花算法，DB层自增兜底）',
    order_id        BIGINT        NOT NULL                 COMMENT '订单 ID',
    order_no        VARCHAR(32)   NOT NULL                 COMMENT '订单编号',
    payment_id      BIGINT        DEFAULT NULL             COMMENT '原支付记录 ID',
    refund_amount   DECIMAL(10,2) NOT NULL                 COMMENT '退款金额',
    reason          VARCHAR(256)  DEFAULT NULL             COMMENT '退款原因',
    status          VARCHAR(16)   NOT NULL DEFAULT '成功'  COMMENT '审核中/成功/驳回',
    operator_id     BIGINT        NOT NULL                 COMMENT '操作人 ID',
    create_time     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_order_id (order_id)
) ENGINE=InnoDB COMMENT='退款记录表';
