package com.lhf.hotel.finance.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OccupancyTrendVO {

    @Schema(description = "日期列表")
    private List<String> dates;

    @Schema(description = "入住率列表")
    private List<String> occupancyRates;

    @Schema(description = "可用房间数")
    private Integer availableRooms;

    @Schema(description = "平均入住率")
    private String avgOccupancyRate;
}
