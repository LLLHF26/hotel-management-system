package com.lhf.hotel.finance.controller;

import com.lhf.hotel.common.result.PageResult;
import com.lhf.hotel.common.result.Result;
import com.lhf.hotel.finance.model.vo.RefundDetailVO;
import com.lhf.hotel.common.model.vo.RefundRecordVO;
import com.lhf.hotel.finance.service.FinanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/finance")
@RequiredArgsConstructor
public class RefundController {

    private final FinanceService financeService;

    @GetMapping("/refunds")
    public Result<PageResult<RefundRecordVO>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        return Result.ok(financeService.refundList(page, size, startDate, endDate, status, keyword));
    }

    @GetMapping("/refund/{id}")
    public Result<RefundDetailVO> detail(@PathVariable Long id) {
        return Result.ok(financeService.refundDetail(id));
    }

    @PostMapping("/refund/add")
    public Result<Void> add(@RequestBody RefundRecordVO refundRecordVO) {
        financeService.addRefundRecord(refundRecordVO);
        return Result.ok();
    }
}
