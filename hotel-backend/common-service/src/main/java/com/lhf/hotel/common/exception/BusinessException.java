package com.lhf.hotel.common.exception;

import com.lhf.hotel.common.constant.ErrorCode;
import lombok.Getter;

/**
 * 业务异常 —— 抛出后由 GlobalExceptionHandler 统一拦截返回
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getDefaultMsg());
        this.code = errorCode.getCode();
    }

    public BusinessException(int code, String msg) {
        super(msg);
        this.code = code;
    }

    public BusinessException(String msg) {
        super(msg);
        this.code = ErrorCode.BAD_REQUEST.getCode();
    }
}
