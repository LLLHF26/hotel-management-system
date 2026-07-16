package com.lhf.hotel.finance.model.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class MonthSummary {

    private String month;
    private BigDecimal roomRevenue;
    private BigDecimal extraRevenue;
    private BigDecimal totalRevenue;
    private Integer orderCount;
    private BigDecimal avgDailyRevenue;
}
