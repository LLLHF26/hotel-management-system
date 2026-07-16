package com.lhf.hotel.common.event.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "eventType")
@JsonSubTypes({
    @JsonSubTypes.Type(value = OrderCreatedEvent.class, name = "CREATED"),
    @JsonSubTypes.Type(value = OrderPaidEvent.class, name = "PAID"),
    @JsonSubTypes.Type(value = OrderCheckInEvent.class, name = "CHECK_IN"),
    @JsonSubTypes.Type(value = OrderCheckoutEvent.class, name = "CHECK_OUT"),
    @JsonSubTypes.Type(value = OrderCancelledEvent.class, name = "CANCELLED"),
    @JsonSubTypes.Type(value = OrderCancelledEvent.class, name = "AUTO_CANCELLED"),
    @JsonSubTypes.Type(value = OrderRefundEvent.class, name = "REFUNDED"),
    @JsonSubTypes.Type(value = OrderExtendedEvent.class, name = "EXTENDED"),
    @JsonSubTypes.Type(value = OrderRoomChangedEvent.class, name = "ROOM_CHANGED")
})
public class OrderEvent {
    private Long orderId;
    private String orderNo;
    private OrderEventType eventType;
    private String status;
    private Long customerId;
    private String customerName;
    private Long roomId;
    private String roomNumber;
    private LocalDateTime timestamp;
}
