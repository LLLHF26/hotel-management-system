package com.lhf.hotel.order.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class OrderCancelDTO {

    @Schema(description = "取消原因")
    private String reason;
}
