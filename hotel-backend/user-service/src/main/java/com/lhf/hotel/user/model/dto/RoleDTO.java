package com.lhf.hotel.user.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RoleDTO {

    @Schema(description = "角色")
    @NotBlank(message = "角色不能为空")
    private String role;
}
