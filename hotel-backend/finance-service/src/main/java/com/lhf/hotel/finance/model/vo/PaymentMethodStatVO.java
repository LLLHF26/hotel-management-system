package com.lhf.hotel.finance.model.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PaymentMethodStatVO {

    private String method;
    private String methodName;
    private BigDecimal amount;
    private Integer count;
    private String rate;
}
