package com.lhf.hotel.order.feign.fallback;

import com.lhf.hotel.common.result.Result;
import com.lhf.hotel.common.model.vo.RefundRecordVO;
import com.lhf.hotel.order.feign.financeService.FinanceRefundFeignClient;
import org.springframework.stereotype.Component;

@Component
public class FinanceRefundFeignClientFallback implements FinanceRefundFeignClient {
    @Override
    public Result<Void> add(RefundRecordVO refundRecordVO) {
        return Result.fail("添加退款记录失败");
    }
}
