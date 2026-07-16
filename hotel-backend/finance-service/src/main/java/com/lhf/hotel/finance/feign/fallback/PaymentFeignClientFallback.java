package com.lhf.hotel.finance.feign.fallback;

import com.lhf.hotel.common.result.Result;
import com.lhf.hotel.finance.feign.orderService.PaymentFeignClient;
import org.springframework.stereotype.Component;
import com.lhf.hotel.common.model.vo.PaymentVO;

import java.util.List;

@Component
public class PaymentFeignClientFallback implements PaymentFeignClient {
    @Override
    public Result<List<PaymentVO>> getPayments(Long id) {
        return Result.fail("订单服务异常");
    }

    @Override
    public Result<List<PaymentVO>> getPaymentsByTime(String startTime, String endTime) {
        return Result.fail("订单服务异常");
    }
}
