package com.lhf.hotel.order.feign.fallback;

import com.lhf.hotel.common.dto.RoomStatusChangeDTO;
import com.lhf.hotel.common.result.PageResult;
import com.lhf.hotel.common.result.Result;
import com.lhf.hotel.order.feign.roomServiceFeignClient.RoomFeignClient;
import com.lhf.hotel.room.model.vo.RoomVO;
import org.springframework.stereotype.Component;

@Component
public class RoomServiceFeignClientFallback implements RoomFeignClient {

    @Override
    public Result<RoomVO> detail(Long id) {
        return null;
    }

    @Override
    public Result<PageResult<RoomVO>> list(int page, int size, Long roomTypeId, String status, Integer floor, String keyword) {
        return null;
    }

    @Override
    public Result<Void> changeStatus(Long id, RoomStatusChangeDTO dto) {
        return null;
    }
}
