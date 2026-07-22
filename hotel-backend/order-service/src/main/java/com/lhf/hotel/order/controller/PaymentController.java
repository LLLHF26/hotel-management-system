package com.lhf.hotel.order.controller;

import com.lhf.hotel.common.result.Result;
import com.lhf.hotel.order.model.dto.PayDTO;
import com.lhf.hotel.order.model.dto.RefundDTO;
import com.lhf.hotel.common.model.vo.PaymentVO;
import com.lhf.hotel.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "支付管理", description = "订单支付、退款、支付记录查询")
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class PaymentController {

    private final OrderService orderService;

    /** 订单支付 */
    @Operation(summary = "订单支付")
    @PostMapping("/{id}/pay")
    public Result<Map<String, Object>> pay(@Parameter(description = "订单ID") @PathVariable Long id, @Parameter(description = "支付信息") @Valid @RequestBody PayDTO dto) {
        return Result.ok("支付成功", orderService.pay(id, dto));
    }

    /** 订单退款 */
    @Operation(summary = "订单退款")
    @PostMapping("/{id}/refund")
    public Result<Map<String, Object>> refund(@Parameter(description = "订单ID") @PathVariable Long id, @Parameter(description = "退款信息") @Valid @RequestBody RefundDTO dto) {
        return Result.ok("退款成功", orderService.refund(id, dto));
    }

    /** 订单支付记录 */
    @Operation(summary = "订单支付记录")
    @GetMapping("/{id}/payments")
    public Result<List<PaymentVO>> getPayments(@Parameter(description = "订单ID") @PathVariable Long id) {
        return Result.ok(orderService.getPayments(id));
    }

    /** 按时间段查询支付记录 */
    @Operation(summary = "按时间段查询支付记录")
    @GetMapping("/payments/time")
    public Result<List<PaymentVO>> getPaymentsByTime(
            @Parameter(description = "开始时间") @RequestParam String startTime,
            @Parameter(description = "结束时间") @RequestParam String endTime) {
        return Result.ok(orderService.getPaymentsByTime(startTime, endTime));
    }

}
