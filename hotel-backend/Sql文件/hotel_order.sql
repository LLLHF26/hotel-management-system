-- ============================================================
-- hotel_order — order-service 数据库
-- 订单表 / 消费明细表 / 支付记录表
-- ============================================================

DROP DATABASE IF EXISTS hotel_order;
CREATE DATABASE hotel_order DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE hotel_order;

-- 1. 订单表
CREATE TABLE orders (
    id                BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键（应用层雪花算法，DB层自增兜底）',
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
    version           INT           NOT NULL DEFAULT 0       COMMENT '乐观锁版本号',
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
    id            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键（应用层雪花算法，DB层自增兜底）',
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
    id            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键（应用层雪花算法，DB层自增兜底）',
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

-- 4. 客房商品表（食物 / 饮用水 / 日用品等）
CREATE TABLE product (
    id          BIGINT        NOT NULL COMMENT '主键（雪花算法）',
    category    VARCHAR(20)   NOT NULL DEFAULT '其他'   COMMENT '分类：食物/饮用水/日用品/其他',
    name        VARCHAR(100)  NOT NULL                 COMMENT '商品名称',
    price       DECIMAL(10,2) NOT NULL                 COMMENT '单价（元）',
    image       VARCHAR(255)  DEFAULT NULL             COMMENT '封面图 URL',
    unit        VARCHAR(10)   NOT NULL DEFAULT '份'     COMMENT '计价单位：份/瓶/个/盒',
    stock       INT           NOT NULL DEFAULT -1      COMMENT '库存（-1 表示不限量）',
    status      VARCHAR(10)   NOT NULL DEFAULT '上架'   COMMENT '上架/下架',
    description VARCHAR(500)  DEFAULT NULL             COMMENT '描述',
    sort_order  INT           NOT NULL DEFAULT 0       COMMENT '排序（越小越靠前）',
    create_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_status (status),
    KEY idx_category (category)
) ENGINE=InnoDB COMMENT='客房商品表';

-- 5. 服务呼叫表（客户呼叫人员：打扫 / 送物 / 维修等）
CREATE TABLE service_request (
    id            BIGINT        NOT NULL COMMENT '主键（雪花算法）',
    customer_id   BIGINT        NOT NULL                 COMMENT '发起客户 ID',
    customer_name VARCHAR(50)   DEFAULT NULL             COMMENT '客户姓名（冗余）',
    room_id       BIGINT        DEFAULT NULL             COMMENT '房间 ID',
    room_number   VARCHAR(20)   DEFAULT NULL             COMMENT '房间号（冗余）',
    type          VARCHAR(20)   NOT NULL                 COMMENT '服务类型：打扫/送物/维修/其他',
    remark        VARCHAR(500)  DEFAULT NULL             COMMENT '备注说明',
    status        VARCHAR(20)   NOT NULL DEFAULT '待处理' COMMENT '待处理/已处理/已取消',
    handler_id    BIGINT        DEFAULT NULL             COMMENT '处理人 ID',
    handle_remark VARCHAR(500)  DEFAULT NULL             COMMENT '处理备注',
    handle_time   DATETIME      DEFAULT NULL             COMMENT '处理时间',
    create_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    KEY idx_customer_id (customer_id),
    KEY idx_status (status),
    KEY idx_room_id (room_id)
) ENGINE=InnoDB COMMENT='服务呼叫表';
