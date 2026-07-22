package com.lhf.hotel.user.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("points_log")
public class PointsLog {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long customerId;
    private String type;
    private Integer points;
    private Integer balanceAfter;
    private String reason;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
