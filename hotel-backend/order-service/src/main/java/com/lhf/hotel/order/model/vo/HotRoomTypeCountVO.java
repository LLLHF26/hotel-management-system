package com.lhf.hotel.order.model.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotRoomTypeCountVO {
    private String roomTypeName;
    private Long orderCount;
}
