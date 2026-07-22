package com.lhf.hotel.room.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class MaintenanceCompleteDTO {

    @Schema(description = "维修费用")
    private BigDecimal cost;
}
