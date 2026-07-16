package com.lhf.hotel.user.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StatusDTO {

    @NotNull(message = "状态值不能为空")
    private Integer status;
}
