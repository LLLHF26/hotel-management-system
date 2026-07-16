package com.lhf.hotel.order.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PayDTO {

    @NotNull(message = "支付金额不能为空")
    private BigDecimal amount;

    @NotBlank(message = "支付方式不能为空")
    private String method;
}
