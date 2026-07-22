package com.lhf.hotel.order.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HotRoomTypeCountVO {

    @Schema(description = "房型名称")
    private String roomTypeName;

    @Schema(description = "订单数量")
    private Long orderCount;
}
