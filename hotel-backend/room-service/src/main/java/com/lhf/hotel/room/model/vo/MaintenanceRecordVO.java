package com.lhf.hotel.room.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaintenanceRecordVO {

    @Schema(description = "记录ID")
    private Long id;

    @Schema(description = "房间ID")
    private Long roomId;

    @Schema(description = "房间编号")
    private String roomNumber;

    @Schema(description = "维修原因")
    private String reason;

    @Schema(description = "维修状态")
    private String status;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

    @Schema(description = "维修费用")
    private BigDecimal cost;
}
