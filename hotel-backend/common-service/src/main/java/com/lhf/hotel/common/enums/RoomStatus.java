package com.lhf.hotel.common.enums;

import lombok.Getter;

import java.util.Map;
import java.util.Set;

/**
 * 房间六状态枚举 —— 含合法跳转校验
 */
@Getter
public enum RoomStatus {

    空闲中,
    预订中,
    入住中,
    待清洁中,
    打扫中,
    维修中;

    /** 状态 → 允许跳转的目标状态集合 */
    private static final Map<RoomStatus, Set<RoomStatus>> ALLOWED_TRANSITIONS = Map.of(
            空闲中,   Set.of(预订中, 维修中),
            预订中,   Set.of(入住中, 空闲中),
            入住中,   Set.of(待清洁中),
            待清洁中, Set.of(打扫中),
            打扫中,   Set.of(空闲中),
            维修中,   Set.of(空闲中)
    );

    public boolean canTransitionTo(RoomStatus target) {
        Set<RoomStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(this, Set.of());
        return allowed.contains(target);
    }
}
