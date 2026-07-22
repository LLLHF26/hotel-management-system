package com.lhf.hotel.user.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class CustomerUpdateDTO {

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "身份证号")
    private String idCard;

    @Schema(description = "性别")
    private Integer gender;

    @Schema(description = "头像")
    private String avatar;
}
