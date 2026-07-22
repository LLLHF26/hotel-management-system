package com.lhf.hotel.order.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CheckOutDTO {

    @Schema(description = "退还押金")
    private BigDecimal refundDeposit;
}
