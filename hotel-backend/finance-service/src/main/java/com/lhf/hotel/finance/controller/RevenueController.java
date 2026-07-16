package com.lhf.hotel.finance.controller;

import com.lhf.hotel.common.result.PageResult;
import com.lhf.hotel.common.result.Result;
import com.lhf.hotel.finance.model.vo.*;
import com.lhf.hotel.finance.service.FinanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/finance/revenue")
@RequiredArgsConstructor
@Slf4j
public class RevenueController {

    private final FinanceService financeService;

    @GetMapping("/summary")
    public Result<RevenueSummaryVO> summary() {
        log.info("RevenueController.summary");
        return Result.ok(financeService.revenueSummary());
    }

    @GetMapping("/daily")
    public Result<DailyTrendVO> daily(@RequestParam String startDate,
                                      @RequestParam String endDate) {
        return Result.ok(financeService.revenueDaily(startDate, endDate));
    }

    @GetMapping("/monthly")
    public Result<List<MonthlyRevenueItemVO>> monthly(@RequestParam(required = false) Integer year) {
        return Result.ok(financeService.revenueMonthly(year));
    }

    @GetMapping("/range")
    public Result<RevenueRangeVO> range(@RequestParam String startDate,
                                        @RequestParam String endDate) {
        return Result.ok(financeService.revenueRange(startDate, endDate));
    }

    @GetMapping("/detail")
    public Result<PageResult<RevenueDetailVO>> detail(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String roomNumber) {
        return Result.ok(financeService.revenueDetail(page, size, startDate, endDate,
                paymentMethod, orderNo, roomNumber));
    }

    @GetMapping("/by-payment-method")
    public Result<List<PaymentMethodStatVO>> byPaymentMethod(@RequestParam String startDate,
                                                             @RequestParam String endDate) {
        return Result.ok(financeService.revenueByPaymentMethod(startDate, endDate));
    }

    @GetMapping("/by-room-type")
    public Result<List<RoomTypeRevenueVO>> byRoomType(@RequestParam String startDate,
                                                      @RequestParam String endDate) {
        return Result.ok(financeService.revenueByRoomType(startDate, endDate));
    }
}
