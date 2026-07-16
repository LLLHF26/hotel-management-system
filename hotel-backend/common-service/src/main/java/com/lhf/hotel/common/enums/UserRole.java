package com.lhf.hotel.common.enums;

import lombok.Getter;

/**
 * 用户角色
 */
@Getter
public enum UserRole {

    ADMIN("管理员"),
    FRONT_DESK("前台"),
    CLEANER("保洁员");

    private final String label;

    UserRole(String label) {
        this.label = label;
    }
}
