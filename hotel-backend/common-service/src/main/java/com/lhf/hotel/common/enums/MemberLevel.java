package com.lhf.hotel.common.enums;

import lombok.Getter;

/**
 * 会员等级
 */
@Getter
public enum MemberLevel {

    NORMAL("普通"),
    SILVER("银卡"),
    GOLD("金卡"),
    DIAMOND("钻卡");

    private final String label;

    MemberLevel(String label) {
        this.label = label;
    }

    /** 根据累计消费金额确定等级 */
    public static MemberLevel fromConsumed(double totalConsumed) {
        if (totalConsumed >= 50000) return DIAMOND;
        if (totalConsumed >= 20000) return GOLD;
        if (totalConsumed >= 5000)  return SILVER;
        return NORMAL;
    }
}
