package com.lhf.hotel.user.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StatusDTO {

    @Schema(description = "状态值")
    @NotNull(message = "状态值不能为空")
    private Integer status;
}
