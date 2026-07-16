package com.lhf.hotel.finance.model.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RevenueSummaryVO {

    private TodaySummary today;
    private MonthSummary thisMonth;
    private YearSummary thisYear;
}
