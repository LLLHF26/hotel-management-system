package com.lhf.hotel.user.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerVO {

    @Schema(description = "客户ID")
    private Long id;

    @Schema(description = "真实姓名")
    private String realName;

    @Schema(description = "身份证号")
    private String idCard;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "性别")
    private Integer gender;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "积分")
    private Integer points;

    @Schema(description = "累计消费")
    private BigDecimal totalConsumed;

    @Schema(description = "会员等级")
    private String memberLevel;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
