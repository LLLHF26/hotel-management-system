package com.lhf.hotel.order.feign.userService;

import com.lhf.hotel.common.result.Result;
import com.lhf.hotel.order.feign.fallback.UserServiceFeignClientFallback;
import com.lhf.hotel.user.model.vo.CustomerVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(value = "user-service", contextId = "orderUserFeignClient", fallback = UserServiceFeignClientFallback.class)
public interface UserFeignClient {
    /** 会员详情 */
    @GetMapping("/api/customer/{id}")
    public Result<CustomerVO> detail(@PathVariable Long id);

    /** 增加积分 */
    @PostMapping("/api/customer/{id}/points/add")
    public Result<Void> addPoints(@PathVariable Long id,
                                   @RequestParam Integer points,
                                   @RequestParam String reason);

    /** 增加累计消费 */
    @PostMapping("/api/customer/{id}/consumed/add")
    public Result<Void> addConsumed(@PathVariable Long id,
                                     @RequestParam java.math.BigDecimal amount);
}
