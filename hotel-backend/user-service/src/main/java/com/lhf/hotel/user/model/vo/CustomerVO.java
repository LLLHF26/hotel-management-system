package com.lhf.hotel.user.model.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class CustomerVO {

    private Long id;
    private String realName;
    private String idCard;
    private String phone;
    private Integer gender;
    private String avatar;
    private Integer points;
    private BigDecimal totalConsumed;
    private String memberLevel;
    private Integer status;
    private LocalDateTime createTime;
}
