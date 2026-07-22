package com.lhf.hotel.finance.controller;

import com.lhf.hotel.common.result.PageResult;
import com.lhf.hotel.common.result.Result;
import com.lhf.hotel.finance.model.vo.RefundDetailVO;
import com.lhf.hotel.common.model.vo.RefundRecordVO;
import com.lhf.hotel.finance.service.FinanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/finance")
@RequiredArgsConstructor
@Tag(name = "退款管理", description = "退款记录列表、退款详情、新增退款")
public class RefundController {

    private final FinanceService financeService;

    /** 退款记录列表 */
    @Operation(summary = "退款记录列表")
    @GetMapping("/refunds")
    public Result<PageResult<RefundRecordVO>> list(
            @Parameter(description = "页码（默认1）") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页条数（默认10）") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate,
            @Parameter(description = "退款状态") @RequestParam(required = false) String status,
            @Parameter(description = "搜索关键字") @RequestParam(required = false) String keyword) {
        return Result.ok(financeService.refundList(page, size, startDate, endDate, status, keyword));
    }

    /** 退款详情 */
    @Operation(summary = "退款详情")
    @GetMapping("/refund/{id}")
    public Result<RefundDetailVO> detail(@Parameter(description = "退款记录ID") @PathVariable Long id) {
        return Result.ok(financeService.refundDetail(id));
    }

    /** 新增退款记录 */
    @Operation(summary = "新增退款记录")
    @PostMapping("/refund/add")
    public Result<Void> add(@Parameter(description = "退款记录信息") @RequestBody RefundRecordVO refundRecordVO) {
        financeService.addRefundRecord(refundRecordVO);
        return Result.ok();
    }
}
