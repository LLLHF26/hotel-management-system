package com.lhf.hotel.finance.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OccupancyByTypeVO {

    @Schema(description = "房型名称")
    private String roomTypeName;

    @Schema(description = "总房间数")
    private Integer totalRooms;

    @Schema(description = "已入住间夜数")
    private Integer occupiedNights;

    @Schema(description = "总间夜数")
    private Integer totalNights;

    @Schema(description = "入住率")
    private String occupancyRate;
}
