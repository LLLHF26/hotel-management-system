package com.lhf.hotel.order.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ExtraDTO {

    @NotBlank(message = "项目名称不能为空")
    private String itemName;

    @NotNull(message = "金额不能为空")
    private BigDecimal amount;

    private Integer quantity = 1;
}
