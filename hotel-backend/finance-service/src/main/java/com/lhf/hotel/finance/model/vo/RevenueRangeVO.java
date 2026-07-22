package com.lhf.hotel.finance.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueRangeVO {

    @Schema(description = "开始日期")
    private String startDate;

    @Schema(description = "结束日期")
    private String endDate;

    @Schema(description = "房费收入")
    private BigDecimal roomRevenue;

    @Schema(description = "额外消费收入")
    private BigDecimal extraRevenue;

    @Schema(description = "总收入")
    private BigDecimal totalRevenue;

    @Schema(description = "订单数")
    private Integer orderCount;

    @Schema(description = "入住数")
    private Integer checkInCount;

    @Schema(description = "退房数")
    private Integer checkOutCount;

    @Schema(description = "平均入住率")
    private String avgOccupancyRate;

    @Schema(description = "支付方式分项")
    private Map<String, BigDecimal> paymentBreakdown;
}
