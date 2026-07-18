package com.lhf.hotel.room.feign.order;

import com.lhf.hotel.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

@FeignClient(value = "order-service", contextId = "roomOrderStatsFeignClient", fallback = OrderStatsFeignClientFallback.class)
public interface OrderStatsFeignClient {

    @GetMapping("/api/order/stats/hot-room-types")
    Result<List<Map<String, Object>>> getHotRoomTypes(@RequestParam(defaultValue = "6") int topN,
                                                       @RequestParam(defaultValue = "30") int days);
}
