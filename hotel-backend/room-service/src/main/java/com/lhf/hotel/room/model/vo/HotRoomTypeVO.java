package com.lhf.hotel.room.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotRoomTypeVO {

    @Schema(description = "房型ID")
    private Long id;

    @Schema(description = "房型名称")
    private String name;

    @Schema(description = "床型")
    private String bedType;

    @Schema(description = "最大入住人数")
    private Integer maxGuests;

    @Schema(description = "价格")
    private BigDecimal price;

    @Schema(description = "封面图片")
    private String coverImage;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "订单数量")
    private Long orderCount;
}
