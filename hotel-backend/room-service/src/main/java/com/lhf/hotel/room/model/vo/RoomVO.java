package com.lhf.hotel.room.model.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class RoomVO {

    private Long id;
    private String roomNumber;
    private Long roomTypeId;
    private String roomTypeName;
    private Integer floor;
    private String status;
    private BigDecimal price;
    private String description;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    /** 打扫中时展示 */
    private String cleanerName;
    private LocalDateTime taskStartTime;

    /** 当前打扫/维修信息（非打扫中/维修中时为 null） */
    private TaskInfo currentTask;
}
