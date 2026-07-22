package com.lhf.hotel.room.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RoomSaveDTO {

    @Schema(description = "房间编号")
    @NotBlank(message = "房间编号不能为空")
    private String roomNumber;

    @Schema(description = "房型ID")
    @NotNull(message = "房型不能为空")
    private Long roomTypeId;

    @Schema(description = "楼层")
    private Integer floor;

    @Schema(description = "价格")
    private BigDecimal price;

    @Schema(description = "描述")
    private String description;
}
