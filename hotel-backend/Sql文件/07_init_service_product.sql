-- =========================================================
-- 服务功能 / 客房商品：缺失表与配置初始化（幂等，可重复执行）
-- 原因：运行时报 Table 'hotel_order.product' doesn't exist，
--       说明本功能新增的两张表尚未在数据库中创建。
-- 执行方式（二选一）：
--   1) 在 MySQL 客户端直接 source 本文件；
--   2) 命令行：
--      mysql -u<user> -p hotel_order   < 07_init_service_product.sql
--      （文件内已 USE 切换库，整库脚本一次执行即可）
-- =========================================================

USE hotel_order;

-- 1) 客房商品表
CREATE TABLE IF NOT EXISTS product (
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

-- 2) 服务呼叫表
CREATE TABLE IF NOT EXISTS service_request (
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

-- 3) 积分抵扣配置（写入 system 库），INSERT IGNORE 保证重复执行不报错
USE hotel_system;

INSERT IGNORE INTO system_setting (`key`, value, description) VALUES
    ('points_discount_ratio',   '0.1',  '1 积分可抵扣的金额（元），用于客房商品下单抵扣'),
    ('points_discount_enabled', 'true', '是否启用积分抵扣（true/false）');
