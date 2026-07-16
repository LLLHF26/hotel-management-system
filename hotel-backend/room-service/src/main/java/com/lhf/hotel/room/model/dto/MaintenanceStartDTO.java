package com.lhf.hotel.room.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MaintenanceStartDTO {

    @NotBlank(message = "维修原因不能为空")
    private String reason;
}
