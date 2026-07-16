package com.lhf.hotel.finance.controller;

import com.lhf.hotel.common.result.Result;
import com.lhf.hotel.finance.service.FinanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/finance/report")
@RequiredArgsConstructor
public class ReportController {

    private final FinanceService financeService;

    @GetMapping("/export")
    public Result<Void> export(@RequestParam String type,
                               @RequestParam String startDate,
                               @RequestParam String endDate,
                               @RequestParam(defaultValue = "xlsx") String format) {
        financeService.exportReport(type, startDate, endDate, format);
        return Result.ok(null);
    }
}
