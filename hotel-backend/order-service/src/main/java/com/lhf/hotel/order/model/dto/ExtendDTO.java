package com.lhf.hotel.order.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ExtendDTO {

    @Schema(description = "续住天数")
    @NotNull(message = "续住天数不能为空")
    @Min(value = 1, message = "续住天数至少为1")
    private Integer extendDays;
}
