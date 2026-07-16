package com.lhf.hotel.room.model.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/** 打扫任务 or 维修任务的摘要信息 */
@Data
@Builder
public class TaskInfo {

    private Long taskId;
    private String type;              // cleaning / maintenance
    private String cleanerName;       // 打扫时返回
    private String reason;            // 维修时返回
    private LocalDateTime startTime;
    private Integer remainMinutes;    // 预计剩余分钟数
}
