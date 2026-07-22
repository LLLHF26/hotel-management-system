package com.lhf.hotel.order.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CheckInDTO {

    @Schema(description = "押金")
    private BigDecimal deposit;
}
