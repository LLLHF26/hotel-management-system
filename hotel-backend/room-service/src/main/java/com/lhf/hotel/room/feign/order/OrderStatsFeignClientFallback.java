package com.lhf.hotel.room.feign.order;

import com.lhf.hotel.common.result.Result;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class OrderStatsFeignClientFallback implements OrderStatsFeignClient {

    @Override
    public Result<List<Map<String, Object>>> getHotRoomTypes(int topN, int days) {
        return Result.ok(Collections.emptyList());
    }
}
