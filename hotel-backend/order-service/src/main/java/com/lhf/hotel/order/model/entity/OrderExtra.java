package com.lhf.hotel.order.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("order_extra")
public class OrderExtra {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private String itemName;
    private BigDecimal amount;
    private Integer quantity;
    private Long operatorId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
