package com.lhf.hotel.user.event;

import com.lhf.hotel.common.event.dto.OrderCheckoutEvent;
import com.lhf.hotel.common.event.dto.OrderEvent;
import com.lhf.hotel.user.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventListener {

    private final CustomerService customerService;

    @RabbitListener(queues = "hotel.order.points")
    public void handleOrderCheckout(OrderCheckoutEvent event) {
        if (event.getCustomerId() == null) return;
        log.info("收到退房积分事件: orderNo={}, customerId={}, earnedPoints={}",
                event.getOrderNo(), event.getCustomerId(), event.getEarnedPoints());
        try {
            if (event.getEarnedPoints() != null && event.getEarnedPoints() > 0) {
                customerService.addPoints(event.getCustomerId(), event.getEarnedPoints(),
                        "订单" + event.getOrderNo() + "退房积分");
            }
            if (event.getTotalAmount() != null) {
                customerService.addConsumed(event.getCustomerId(), event.getTotalAmount());
            }
        } catch (Exception e) {
            log.error("处理退房积分失败: orderNo={}, customerId={}", event.getOrderNo(), event.getCustomerId(), e);
            throw e;
        }
    }

    @RabbitListener(queues = "hotel.order.notify")
    public void handleNotification(OrderEvent event) {
        log.info("收到订单通知: orderNo={}, eventType={}, status={}",
                event.getOrderNo(), event.getEventType(), event.getStatus());
    }
}
