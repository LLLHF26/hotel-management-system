package com.lhf.hotel.room.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("cleaning_task")
public class CleaningTask {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long roomId;
    private String roomNumber;
    private Long cleanerId;
    private String cleanerName;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
