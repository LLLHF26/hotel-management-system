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

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class PaymentController {

    private final OrderService orderService;

    @PostMapping("/{id}/pay")
    public Result<Map<String, Object>> pay(@PathVariable Long id, @Valid @RequestBody PayDTO dto) {
        return Result.ok("支付成功", orderService.pay(id, dto));
    }

    @PostMapping("/{id}/refund")
    public Result<Map<String, Object>> refund(@PathVariable Long id, @Valid @RequestBody RefundDTO dto) {
        return Result.ok("退款成功", orderService.refund(id, dto));
    }

    @GetMapping("/{id}/payments")
    public Result<List<PaymentVO>> getPayments(@PathVariable Long id) {
        return Result.ok(orderService.getPayments(id));
    }

    @GetMapping("/payments/time")
    public Result<List<PaymentVO>> getPaymentsByTime(@RequestParam String startTime, @RequestParam String endTime) {
        return Result.ok(orderService.getPaymentsByTime(startTime, endTime));
    }

}
