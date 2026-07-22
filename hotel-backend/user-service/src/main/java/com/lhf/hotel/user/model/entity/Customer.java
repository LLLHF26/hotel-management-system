package com.lhf.hotel.user.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("customer")
public class Customer {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String realName;
    private String idCard;
    private String phone;
    private String password;
    private Integer gender;
    private String avatar;
    private Integer points;
    private BigDecimal totalConsumed;
    private String memberLevel;
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
