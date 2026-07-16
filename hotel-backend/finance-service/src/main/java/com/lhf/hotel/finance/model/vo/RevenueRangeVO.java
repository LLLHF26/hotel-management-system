package com.lhf.hotel.finance.model.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
public class RevenueRangeVO {

    private String startDate;
    private String endDate;
    private BigDecimal roomRevenue;
    private BigDecimal extraRevenue;
    private BigDecimal totalRevenue;
    private Integer orderCount;
    private Integer checkInCount;
    private Integer checkOutCount;
    private String avgOccupancyRate;
    private Map<String, BigDecimal> paymentBreakdown;
}
