package com.lhf.hotel.finance.feign.roomService;

import com.lhf.hotel.common.result.PageResult;
import com.lhf.hotel.common.result.Result;
import com.lhf.hotel.finance.feign.fallback.RoomTypeFeignClientFallback;
import com.lhf.hotel.room.model.vo.RoomTypeVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "room-service", contextId = "financeRoomTypeFeignClient", fallback = RoomTypeFeignClientFallback.class)
public interface RoomTypeFeignClient {
    /** 房型列表（客户端免登录） */
    @GetMapping("/api/room/type/list")
    public Result<PageResult<RoomTypeVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword);
}
