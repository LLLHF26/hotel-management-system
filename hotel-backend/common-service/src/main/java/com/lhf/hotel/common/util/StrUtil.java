package com.lhf.hotel.common.util;

/**
 * 字符串工具
 */
public final class StrUtil {

    private StrUtil() {
    }

    /** 手机号脱敏：139****0001 */
    public static String maskPhone(String phone) {
        if (phone == null || phone.length() != 11) return phone;
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    /** 身份证脱敏：320xxx********xxxx */
    public static String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 8) return idCard;
        return idCard.substring(0, 3) + "xxx********" + idCard.substring(idCard.length() - 4);
    }

    /** 是否为空或空白 */
    public static boolean isBlank(String str) {
        return str == null || str.isBlank();
    }

    /** 是否非空且非空白 */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }
}
