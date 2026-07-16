package com.lhf.hotel.finance.feign.roomService;

import com.lhf.hotel.common.result.PageResult;
import com.lhf.hotel.common.result.Result;
import com.lhf.hotel.finance.feign.fallback.RoomFeignClientFallback;
import com.lhf.hotel.room.model.vo.RoomVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "room-service", contextId = "financeRoomFeignClient", fallback = RoomFeignClientFallback.class)
public interface RoomFeignClient {
    /** 房间列表 */
    @GetMapping("/api/room/list")
    public Result<PageResult<RoomVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long roomTypeId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer floor,
            @RequestParam(required = false) String keyword);
}
