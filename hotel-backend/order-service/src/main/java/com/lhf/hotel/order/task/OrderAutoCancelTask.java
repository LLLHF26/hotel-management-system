package com.lhf.hotel.order.task;

import com.lhf.hotel.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderAutoCancelTask {

    private final OrderService orderService;

    @Scheduled(fixedDelay = 30_000)
    public void autoCancelExpiredOrders() {
        try {
            int count = orderService.autoCancelExpired(30);
            if (count > 0) {
                log.info("自动取消超时订单完成, 本次取消: {} 笔", count);
            }
        } catch (Exception e) {
            log.error("自动取消超时订单异常", e);
        }
    }
}
