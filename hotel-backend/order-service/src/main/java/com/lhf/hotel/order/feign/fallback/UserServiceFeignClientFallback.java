package com.lhf.hotel.order.feign.fallback;

import com.lhf.hotel.common.result.Result;
import com.lhf.hotel.order.feign.userService.UserFeignClient;
import com.lhf.hotel.user.model.vo.CustomerVO;
import org.springframework.stereotype.Component;

@Component
public class UserServiceFeignClientFallback implements UserFeignClient {

    @Override
    public Result<CustomerVO> detail(Long id) {
        return Result.fail("用户服务异常");
    }

    @Override
    public Result<Void> addPoints(Long id, Integer points, String reason) {
        return Result.fail("用户服务异常");
    }

    @Override
    public Result<Void> addConsumed(Long id, java.math.BigDecimal amount) {
        return Result.fail("用户服务异常");
    }
}
