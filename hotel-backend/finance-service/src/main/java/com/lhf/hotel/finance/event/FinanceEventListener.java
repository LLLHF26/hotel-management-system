package com.lhf.hotel.finance.event;

import com.lhf.hotel.common.event.dto.OrderEvent;
import com.lhf.hotel.common.event.dto.OrderPaidEvent;
import com.lhf.hotel.common.event.dto.OrderRefundEvent;
import com.lhf.hotel.finance.service.FinanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FinanceEventListener {

    private final FinanceService financeService;

    @RabbitListener(queues = "hotel.order.finance")
    public void handleFinanceEvent(OrderEvent event) {
        switch (event.getEventType()) {
            case PAID -> {
                OrderPaidEvent paid = (OrderPaidEvent) event;
                log.info("收到支付事件: orderNo={}, amount={}, method={}",
                        paid.getOrderNo(), paid.getPaidAmount(), paid.getPaymentMethod());
            }
            case REFUNDED -> {
                OrderRefundEvent refund = (OrderRefundEvent) event;
                log.info("收到退款事件: orderNo={}, amount={}",
                        refund.getOrderNo(), refund.getRefundAmount());
            }
            default -> log.info("收到财务事件: orderNo={}, eventType={}",
                    event.getOrderNo(), event.getEventType());
        }
    }
}
