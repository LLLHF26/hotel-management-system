package com.lhf.hotel.room.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

/** 打扫任务 or 维修任务的摘要信息 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskInfo {

    @Schema(description = "任务ID")
    private Long taskId;

    @Schema(description = "任务类型")
    private String type;              // cleaning / maintenance

    @Schema(description = "保洁员姓名")
    private String cleanerName;       // 打扫时返回

    @Schema(description = "维修原因")
    private String reason;            // 维修时返回

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "预计剩余分钟数")
    private Integer remainMinutes;    // 预计剩余分钟数
}
