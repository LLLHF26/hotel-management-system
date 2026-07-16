package com.lhf.hotel.finance.feign.fallback;


import com.lhf.hotel.common.result.PageResult;
import com.lhf.hotel.common.result.Result;
import com.lhf.hotel.finance.feign.roomService.RoomFeignClient;
import com.lhf.hotel.room.model.vo.RoomVO;
import org.springframework.stereotype.Component;

@Component
public class RoomFeignClientFallback implements RoomFeignClient {
    @Override
    public Result<PageResult<RoomVO>> list(int page, int size, Long roomTypeId, String status, Integer floor, String keyword) {
        return Result.fail("room-service调用失败");
    }
}
