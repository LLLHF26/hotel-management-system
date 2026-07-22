package com.lhf.hotel.order.feign.systemService;

import com.lhf.hotel.common.result.Result;
import com.lhf.hotel.order.feign.fallback.SystemSettingFeignClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@FeignClient(value = "system-service", contextId = "orderSystemFeignClient", fallback = SystemSettingFeignClientFallback.class)
public interface SystemSettingFeignClient {

    /** 获取全部系统设置（含积分抵扣比例） */
    @GetMapping("/api/system/settings")
    Result<List<Map<String, Object>>> listSettings();
}
