package com.lhf.hotel.order.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ServiceRequestDTO {

    @Schema(description = "服务类型：打扫 / 送物 / 维修 / 其他")
    @NotBlank(message = "服务类型不能为空")
    private String type;

    @Schema(description = "备注说明")
    private String remark;
}
