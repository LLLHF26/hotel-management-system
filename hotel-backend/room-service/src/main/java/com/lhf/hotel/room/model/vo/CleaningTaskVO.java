package com.lhf.hotel.room.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CleaningTaskVO {

    @Schema(description = "任务ID")
    private Long id;

    @Schema(description = "房间ID")
    private Long roomId;

    @Schema(description = "房间编号")
    private String roomNumber;

    @Schema(description = "房型名称")
    private String roomTypeName;

    @Schema(description = "保洁员ID")
    private Long cleanerId;

    @Schema(description = "保洁员姓名")
    private String cleanerName;

    @Schema(description = "任务状态")
    private String status;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "打扫耗时（分钟）")
    private Integer durationMinutes;   // 打扫耗时（已完成时）

    @Schema(description = "已耗时（分钟）")
    private Integer elapsedMinutes;    // 已耗时（打扫中时）
}
