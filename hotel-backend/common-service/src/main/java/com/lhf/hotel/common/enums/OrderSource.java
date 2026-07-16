package com.lhf.hotel.common.enums;

import lombok.Getter;

/**
 * 订单来源
 */
@Getter
public enum OrderSource {

    ONLINE("线上预订"),
    WALK_IN("到店散客"),
    FRONT_DESK("前台代订");

    private final String label;

    OrderSource(String label) {
        this.label = label;
    }
}
