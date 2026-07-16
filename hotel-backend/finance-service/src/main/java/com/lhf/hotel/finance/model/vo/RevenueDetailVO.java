package com.lhf.hotel.finance.model.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class RevenueDetailVO {

    private String paymentNo;
    private String orderNo;
    private String roomNumber;
    private String roomTypeName;
    private String customerName;
    private BigDecimal amount;
    private String method;
    private String methodName;
    private String type;
    private LocalDateTime paidAt;
}
