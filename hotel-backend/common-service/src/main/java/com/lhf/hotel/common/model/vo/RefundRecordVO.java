package com.lhf.hotel.common.model.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class RefundRecordVO {

    private Long id;
    private Long orderId;
    private String orderNo;
    private String paymentNo;
    private BigDecimal refundAmount;
    private String reason;
    private String status;
    private String operatorName;
    private LocalDateTime createTime;
}
