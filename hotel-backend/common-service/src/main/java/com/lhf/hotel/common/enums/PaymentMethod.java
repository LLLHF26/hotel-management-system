package com.lhf.hotel.common.enums;

import lombok.Getter;

/**
 * 支付方式
 */
@Getter
public enum PaymentMethod {

    CASH("现金"),
    ALIPAY("支付宝"),
    WECHAT("微信支付"),
    CARD("银行卡");

    private final String label;

    PaymentMethod(String label) {
        this.label = label;
    }
}
