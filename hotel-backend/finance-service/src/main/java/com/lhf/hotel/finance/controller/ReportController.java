package com.lhf.hotel.finance.controller;

import com.lhf.hotel.common.result.Result;
import com.lhf.hotel.finance.service.FinanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/finance/report")
@RequiredArgsConstructor
@Tag(name = "报表管理", description = "导出财务报表")
public class ReportController {

    private final FinanceService financeService;

    /** 导出财务报表 */
    @Operation(summary = "导出财务报表")
    @GetMapping("/export")
    public Result<Void> export(@Parameter(description = "报表类型") @RequestParam String type,
                               @Parameter(description = "开始日期") @RequestParam String startDate,
                               @Parameter(description = "结束日期") @RequestParam String endDate,
                               @Parameter(description = "导出格式（默认xlsx）") @RequestParam(defaultValue = "xlsx") String format) {
        financeService.exportReport(type, startDate, endDate, format);
        return Result.ok(null);
    }
}
