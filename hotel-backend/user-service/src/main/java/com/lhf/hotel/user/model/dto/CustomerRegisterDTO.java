package com.lhf.hotel.user.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CustomerRegisterDTO {

    @Schema(description = "真实姓名")
    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    @Schema(description = "手机号")
    @NotBlank(message = "手机号不能为空")
    private String phone;

    @Schema(description = "密码")
    private String password;

    @Schema(description = "身份证号")
    private String idCard;

    @Schema(description = "性别")
    private Integer gender;
}
