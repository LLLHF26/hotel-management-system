package com.lhf.hotel.common.model.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class PaymentVO {

    private Long id;
    private String paymentNo;
    private BigDecimal amount;
    private String method;
    private String methodName;
    private String status;
    private LocalDateTime paidAt;
}
