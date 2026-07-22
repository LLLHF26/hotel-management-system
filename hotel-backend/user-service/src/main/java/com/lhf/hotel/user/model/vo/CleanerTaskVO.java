package com.lhf.hotel.user.model.vo;

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
public class CleanerTaskVO {

    @Schema(description = "任务ID")
    private Long taskId;

    @Schema(description = "房间ID")
    private Long roomId;

    @Schema(description = "房间号")
    private String roomNumber;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;
}
