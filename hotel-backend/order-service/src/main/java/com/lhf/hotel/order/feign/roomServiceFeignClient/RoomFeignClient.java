package com.lhf.hotel.order.feign.roomServiceFeignClient;

import com.lhf.hotel.common.dto.RoomStatusChangeDTO;
import com.lhf.hotel.common.result.PageResult;
import com.lhf.hotel.common.result.Result;
import com.lhf.hotel.order.feign.fallback.RoomServiceFeignClientFallback;
import com.lhf.hotel.room.model.vo.RoomVO;
import jakarta.validation.Valid;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(value = "room-service", contextId = "orderRoomFeignClient", fallback = RoomServiceFeignClientFallback.class)
public interface RoomFeignClient {
    /** 房间详情（含打扫/维修进度） */
    @GetMapping("/api/room/{id}")
    public Result<RoomVO> detail(@PathVariable Long id);

    /** 房间列表 */
    @GetMapping("/api/room/list")
    public Result<PageResult<RoomVO>> list(@RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "10") int size,
                                            @RequestParam(required = false) Long roomTypeId,
                                            @RequestParam(required = false) String status,
                                            @RequestParam(required = false) Integer floor,
                                            @RequestParam(required = false) String keyword);

    /** 手动变更房态（含合法跳转校验） */
    @PutMapping("/api/room/{id}/status")
    public Result<Void> changeStatus(@PathVariable Long id,
                                     @Valid @RequestBody RoomStatusChangeDTO dto);

}
