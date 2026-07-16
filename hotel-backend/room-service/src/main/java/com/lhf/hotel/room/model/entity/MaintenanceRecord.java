package com.lhf.hotel.room.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("maintenance_record")
public class MaintenanceRecord {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long roomId;
    private String roomNumber;
    private String reason;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal cost;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
