package com.lhf.hotel.order.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PayDTO {

    @Schema(description = "支付金额")
    @NotNull(message = "支付金额不能为空")
    private BigDecimal amount;

    @Schema(description = "支付方式")
    @NotBlank(message = "支付方式不能为空")
    private String method;
}
