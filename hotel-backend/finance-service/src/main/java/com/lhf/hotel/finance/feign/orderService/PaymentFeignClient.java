package com.lhf.hotel.finance.feign.orderService;

import com.lhf.hotel.common.result.Result;
import com.lhf.hotel.finance.feign.fallback.PaymentFeignClientFallback;
import com.lhf.hotel.common.model.vo.PaymentVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(value = "order-service", contextId = "financePaymentFeignClient", fallback = PaymentFeignClientFallback.class)
public interface PaymentFeignClient {
    @GetMapping("/api/order/{id}/payments")
    public Result<List<PaymentVO>> getPayments(@PathVariable Long id);

    @GetMapping("/api/order/payments/time")
    public Result<List<PaymentVO>> getPaymentsByTime(@RequestParam String startTime, @RequestParam String endTime);

}
