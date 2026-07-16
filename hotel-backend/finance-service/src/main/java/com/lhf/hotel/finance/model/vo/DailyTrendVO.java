package com.lhf.hotel.finance.model.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class DailyTrendVO {

    private List<String> dates;
    private List<BigDecimal> roomRevenue;
    private List<BigDecimal> extraRevenue;
    private List<BigDecimal> totalRevenue;
    private List<Integer> orderCount;
}
