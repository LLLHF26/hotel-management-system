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
public class RevenueSummaryVO {

    @Schema(description = "今日营收摘要")
    private TodaySummary today;

    @Schema(description = "昨日营收摘要")
    private TodaySummary yesterday;

    @Schema(description = "本月营收摘要")
    private MonthSummary thisMonth;

    @Schema(description = "本年营收摘要")
    private YearSummary thisYear;
}
