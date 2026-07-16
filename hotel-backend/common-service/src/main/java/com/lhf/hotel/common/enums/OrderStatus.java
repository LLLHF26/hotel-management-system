package com.lhf.hotel.common.enums;

import lombok.Getter;

/**
 * 订单状态
 */
@Getter
public enum OrderStatus {

    待支付,
    已支付,
    已入住,
    已完成,
    已取消,
    已退款
}
