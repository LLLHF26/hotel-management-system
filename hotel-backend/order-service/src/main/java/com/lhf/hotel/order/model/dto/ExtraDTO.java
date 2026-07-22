package com.lhf.hotel.order.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ExtraDTO {

    @Schema(description = "项目名称")
    @NotBlank(message = "项目名称不能为空")
    private String itemName;

    @Schema(description = "金额")
    @NotNull(message = "金额不能为空")
    private BigDecimal amount;

    @Schema(description = "数量")
    private Integer quantity = 1;
}
