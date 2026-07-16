package com.lhf.hotel.order.feign.financeService;

import com.lhf.hotel.common.result.Result;
import com.lhf.hotel.common.model.vo.RefundRecordVO;
import com.lhf.hotel.order.feign.fallback.FinanceRefundFeignClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "finance-service", contextId = "orderFinanceRefundFeignClient", fallback = FinanceRefundFeignClientFallback.class)
public interface FinanceRefundFeignClient {

    @PostMapping("/refund/add")
    public Result<Void> add(@RequestBody RefundRecordVO refundRecordVO);
}
