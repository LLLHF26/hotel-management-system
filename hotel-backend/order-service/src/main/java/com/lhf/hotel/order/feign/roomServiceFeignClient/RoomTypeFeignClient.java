package com.lhf.hotel.order.feign.roomServiceFeignClient;

import com.lhf.hotel.common.result.Result;
import com.lhf.hotel.order.feign.fallback.RoomServiceFeignClientFallback;
import com.lhf.hotel.room.model.vo.RoomTypeVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(value = "room-service", contextId = "orderRoomTypeFeignClient", fallback = RoomServiceFeignClientFallback.class)
public interface RoomTypeFeignClient {
    /** 房型详情 */
    @GetMapping("/api/room/type/{id}")
    public Result<RoomTypeVO> detail(@PathVariable Long id);
}
