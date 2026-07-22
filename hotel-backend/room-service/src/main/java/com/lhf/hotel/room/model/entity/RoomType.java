package com.lhf.hotel.room.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("room_type")
public class RoomType {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String name;
    private String description;
    private Integer area;
    private String bedType;
    private Integer maxGuests;
    private BigDecimal price;
    private String coverImage;
    private String images;
    private String amenities;
    private Integer sortOrder;

    @TableLogic
    private Integer isDeleted;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
