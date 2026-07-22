package com.lhf.hotel.finance.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyTrendVO {

    @Schema(description = "日期列表")
    private List<String> dates;

    @Schema(description = "房费收入列表")
    private List<BigDecimal> roomRevenue;

    @Schema(description = "额外消费收入列表")
    private List<BigDecimal> extraRevenue;

    @Schema(description = "总收入列表")
    private List<BigDecimal> totalRevenue;

    @Schema(description = "订单数列表")
    private List<Integer> orderCount;
}
