package com.lhf.hotel.common.event.dto;

public enum OrderEventType {
    CREATED,
    PAID,
    CHECK_IN,
    CHECK_OUT,
    CANCELLED,
    AUTO_CANCELLED,
    REFUNDED,
    EXTENDED,
    ROOM_CHANGED
}
