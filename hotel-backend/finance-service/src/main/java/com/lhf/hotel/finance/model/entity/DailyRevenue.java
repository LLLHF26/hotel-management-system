package com.lhf.hotel.finance.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("daily_revenue")
public class DailyRevenue {

    @TableId(type = IdType.AUTO)
    private Long id;
    private LocalDate date;
    private BigDecimal roomRevenue;
    private BigDecimal extraRevenue;
    private BigDecimal totalRevenue;
    private Integer orderCount;
    private Integer checkInCount;
    private Integer checkOutCount;
    private BigDecimal cashAmount;
    private BigDecimal alipayAmount;
    private BigDecimal wechatAmount;
    private BigDecimal cardAmount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
