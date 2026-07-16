package com.lhf.hotel.common.asserts;

import com.lhf.hotel.common.constant.ErrorCode;
import com.lhf.hotel.common.exception.BusinessException;

/**
 * 业务断言 —— 简化前置校验代码
 */
public final class Assert {

    private Assert() {
    }

    /** 对象不为 null */
    public static void notNull(Object obj, String msg) {
        if (obj == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND.getCode(), msg);
        }
    }

    /** 表达式为 true */
    public static void isTrue(boolean expression, String msg) {
        if (!expression) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), msg);
        }
    }

    /** 表达式为 false */
    public static void isFalse(boolean expression, String msg) {
        if (expression) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), msg);
        }
    }

    /** 字符串非空 */
    public static void hasText(String str, String msg) {
        if (str == null || str.isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), msg);
        }
    }
}
