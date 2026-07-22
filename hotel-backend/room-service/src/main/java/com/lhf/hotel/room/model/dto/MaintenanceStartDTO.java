package com.lhf.hotel.room.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MaintenanceStartDTO {

    @Schema(description = "维修原因")
    @NotBlank(message = "维修原因不能为空")
    private String reason;
}
