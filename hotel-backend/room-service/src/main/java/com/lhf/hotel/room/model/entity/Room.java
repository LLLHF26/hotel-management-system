package com.lhf.hotel.room.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("room")
public class Room {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String roomNumber;
    private Long roomTypeId;
    private Integer floor;
    private String status;
    private BigDecimal price;
    private String description;

    @Version
    private Integer version;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
