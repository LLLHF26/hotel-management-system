package com.lhf.hotel.order.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class RefundDTO {

    @Schema(description = "退款金额")
    @NotNull(message = "退款金额不能为空")
    private BigDecimal amount;

    @Schema(description = "退款原因")
    @NotBlank(message = "退款原因不能为空")
    private String reason;
}
