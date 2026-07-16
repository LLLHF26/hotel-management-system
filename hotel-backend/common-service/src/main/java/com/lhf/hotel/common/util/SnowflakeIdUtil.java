package com.lhf.hotel.common.util;

import cn.hutool.core.util.IdUtil;

/**
 * 雪花 ID 工具 —— 生成订单号、支付流水号
 */
public final class SnowflakeIdUtil {

    private SnowflakeIdUtil() {
    }

    /** 获取雪花 ID */
    public static long nextId() {
        return IdUtil.getSnowflakeNextId();
    }

    /** 获取雪花 ID 字符串 */
    public static String nextIdStr() {
        return IdUtil.getSnowflakeNextIdStr();
    }

    /** 生成订单编号：ORD + 日期 + 4位流水 */
    public static String generateOrderNo() {
        return "ORD" + DateUtil.todayStr() + String.format("%04d", nextId() % 10000);
    }

    /** 生成支付流水号：PAY + 日期 + 4位流水 */
    public static String generatePaymentNo() {
        return "PAY" + DateUtil.todayStr() + String.format("%04d", nextId() % 10000);
    }
}
