package com.lhf.hotel.order.feign.fallback;

import com.lhf.hotel.common.result.Result;
import com.lhf.hotel.order.feign.systemService.SystemSettingFeignClient;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class SystemSettingFeignClientFallback implements SystemSettingFeignClient {

    @Override
    public Result<List<Map<String, Object>>> listSettings() {
        return Result.ok(Collections.emptyList());
    }
}
