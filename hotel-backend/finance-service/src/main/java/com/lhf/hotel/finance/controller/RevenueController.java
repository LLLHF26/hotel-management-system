package com.lhf.hotel.finance.controller;

import com.lhf.hotel.common.result.PageResult;
import com.lhf.hotel.common.result.Result;
import com.lhf.hotel.finance.model.vo.*;
import com.lhf.hotel.finance.service.FinanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/finance/revenue")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "营收管理", description = "营收总览、每日趋势、月度统计、营收明细、按支付方式/房型统计")
public class RevenueController {

    private final FinanceService financeService;

    /** 营收总览 */
    @Operation(summary = "营收总览")
    @GetMapping("/summary")
    public Result<RevenueSummaryVO> summary() {
        log.info("RevenueController.summary");
        return Result.ok(financeService.revenueSummary());
    }

    /** 每日营收趋势 */
    @Operation(summary = "每日营收趋势")
    @GetMapping("/daily")
    public Result<DailyTrendVO> daily(@Parameter(description = "开始日期") @RequestParam String startDate,
                                      @Parameter(description = "结束日期") @RequestParam String endDate) {
        return Result.ok(financeService.revenueDaily(startDate, endDate));
    }

    /** 月度营收统计 */
    @Operation(summary = "月度营收统计")
    @GetMapping("/monthly")
    public Result<List<MonthlyRevenueItemVO>> monthly(@Parameter(description = "统计年份（默认当前年份）") @RequestParam(required = false) Integer year) {
        return Result.ok(financeService.revenueMonthly(year));
    }

    /** 时间段营收概览 */
    @Operation(summary = "时间段营收概览")
    @GetMapping("/range")
    public Result<RevenueRangeVO> range(@Parameter(description = "开始日期") @RequestParam String startDate,
                                        @Parameter(description = "结束日期") @RequestParam String endDate) {
        return Result.ok(financeService.revenueRange(startDate, endDate));
    }

    /** 营收明细列表 */
    @Operation(summary = "营收明细列表")
    @GetMapping("/detail")
    public Result<PageResult<RevenueDetailVO>> detail(
            @Parameter(description = "页码（默认1）") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页条数（默认20）") @RequestParam(defaultValue = "20") Integer size,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate,
            @Parameter(description = "支付方式") @RequestParam(required = false) String paymentMethod,
            @Parameter(description = "订单号") @RequestParam(required = false) String orderNo,
            @Parameter(description = "房间号") @RequestParam(required = false) String roomNumber) {
        return Result.ok(financeService.revenueDetail(page, size, startDate, endDate,
                paymentMethod, orderNo, roomNumber));
    }

    /** 按支付方式统计营收 */
    @Operation(summary = "按支付方式统计营收")
    @GetMapping("/by-payment-method")
    public Result<List<PaymentMethodStatVO>> byPaymentMethod(@Parameter(description = "开始日期") @RequestParam String startDate,
                                                             @Parameter(description = "结束日期") @RequestParam String endDate) {
        return Result.ok(financeService.revenueByPaymentMethod(startDate, endDate));
    }

    /** 按房型统计营收 */
    @Operation(summary = "按房型统计营收")
    @GetMapping("/by-room-type")
    public Result<List<RoomTypeRevenueVO>> byRoomType(@Parameter(description = "开始日期") @RequestParam String startDate,
                                                      @Parameter(description = "结束日期") @RequestParam String endDate) {
        return Result.ok(financeService.revenueByRoomType(startDate, endDate));
    }
}
