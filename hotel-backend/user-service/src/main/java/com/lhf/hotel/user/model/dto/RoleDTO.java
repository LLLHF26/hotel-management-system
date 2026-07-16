package com.lhf.hotel.user.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RoleDTO {

    @NotBlank(message = "角色不能为空")
    private String role;
}
