package com.lhf.hotel.finance.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyRevenueItemVO {

    @Schema(description = "月份")
    private String month;

    @Schema(description = "房费收入")
    private BigDecimal roomRevenue;

    @Schema(description = "额外消费收入")
    private BigDecimal extraRevenue;

    @Schema(description = "总收入")
    private BigDecimal totalRevenue;

    @Schema(description = "订单数")
    private Integer orderCount;
}
