package com.lhf.hotel.order.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("orders")
public class Orders {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String orderNo;
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private Long roomId;
    private String roomNumber;
    private String roomTypeName;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private LocalDateTime actualCheckIn;
    private LocalDateTime actualCheckOut;
    private Integer nights;
    private BigDecimal roomPrice;
    private BigDecimal roomTotal;
    private BigDecimal extraTotal;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal deposit;
    private String status;
    private String source;
    private String remark;

    @Version
    private Integer version;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
