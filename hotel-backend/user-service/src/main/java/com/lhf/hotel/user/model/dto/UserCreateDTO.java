package com.lhf.hotel.user.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserCreateDTO {

    @Schema(description = "用户名")
    @NotBlank(message = "用户名不能为空")
    @Size(min = 4, max = 32, message = "用户名长度 4-32 位")
    private String username;

    @Schema(description = "密码")
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 32, message = "密码长度 6-32 位")
    private String password;

    @Schema(description = "真实姓名")
    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "角色")
    @NotBlank(message = "角色不能为空")
    private String role;
}
