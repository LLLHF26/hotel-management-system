package com.lhf.hotel.room.event;

import com.lhf.hotel.common.event.dto.OrderEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RoomEventListener {

    @RabbitListener(queues = "hotel.order.cleaning")
    public void handleCleaning(OrderEvent event) {
        log.info("收到清洁任务事件: orderNo={}, roomNumber={}, eventType={}",
                event.getOrderNo(), event.getRoomNumber(), event.getEventType());
        // 房间状态已在 order-service 中改为 待清洁中，此处可扩展自动分派清洁工
    }
}
