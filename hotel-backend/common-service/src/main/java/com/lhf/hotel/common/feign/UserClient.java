package com.lhf.hotel.common.feign;

import com.lhf.hotel.common.dto.CleanerDTO;
import com.lhf.hotel.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * user-service Feign 接口 —— 供 room-service / order-service 调用
 */
@FeignClient(name = "user-service", contextId = "commonUserClient", path = "/api/user")
public interface UserClient {

    @GetMapping("/cleaner/list")
    Result<List<CleanerDTO>> getCleanerList();

    @GetMapping("/customer/{id}")
    Result<?> getCustomer(@PathVariable Long id);
}
