package com.lhf.hotel.finance.feign.fallback;

import com.lhf.hotel.common.result.PageResult;
import com.lhf.hotel.common.result.Result;
import com.lhf.hotel.finance.feign.roomService.RoomTypeFeignClient;
import com.lhf.hotel.room.model.vo.RoomTypeVO;
import org.springframework.stereotype.Component;

@Component
public class RoomTypeFeignClientFallback implements RoomTypeFeignClient {
    @Override
    public Result<PageResult<RoomTypeVO>> list(int page, int size, String keyword) {
        return Result.fail("获取房型列表失败");
    }
}
