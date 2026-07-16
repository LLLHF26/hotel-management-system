package com.lhf.hotel.finance.feign.fallback;

import com.lhf.hotel.common.result.PageResult;
import com.lhf.hotel.common.result.Result;
import com.lhf.hotel.finance.feign.orderService.OrderFeignClient;
import com.lhf.hotel.common.model.vo.OrderVO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderFeignClientFallback implements OrderFeignClient {
    @Override
    public Result<PageResult<OrderVO>> list(Integer page, Integer size, String status, String customerPhone, String roomNumber, String checkInDate, String source, String startDate, String endDate) {
        return Result.fail("订单服务异常");
    }

    @Override
    public Result<List<OrderVO>> listByTime(String startDate, String endDate) {
        return Result.fail("订单服务异常");
    }

    @Override
    public Result<OrderVO> getById(Long id) {
        return Result.fail("订单服务异常");
    }
}
