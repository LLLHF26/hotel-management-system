package com.lhf.hotel.finance.feign.orderService;

import com.lhf.hotel.common.result.PageResult;
import com.lhf.hotel.common.result.Result;
import com.lhf.hotel.finance.feign.fallback.OrderFeignClientFallback;
import com.lhf.hotel.common.model.vo.OrderVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "order-service", contextId = "financeOrderFeignClient", fallback = OrderFeignClientFallback.class)
public interface OrderFeignClient {
    @GetMapping("/api/order/list")
    public Result<PageResult<OrderVO>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String customerPhone,
            @RequestParam(required = false) String roomNumber,
            @RequestParam(required = false) String checkInDate,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate);

    @GetMapping("/api/order/list/byTime")
    public Result<List<OrderVO>> listByTime(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate);

    @GetMapping("/api/order/{id}")
    public Result<OrderVO> getById(@PathVariable Long id);
}
