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
public class RoomVO {

    @Schema(description = "房间ID")
    private Long id;

    @Schema(description = "房间编号")
    private String roomNumber;

    @Schema(description = "房型ID")
    private Long roomTypeId;

    @Schema(description = "房型名称")
    private String roomTypeName;

    @Schema(description = "楼层")
    private Integer floor;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "价格")
    private BigDecimal price;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;

    /** 打扫中时展示 */
    @Schema(description = "保洁员姓名")
    private String cleanerName;

    @Schema(description = "任务开始时间")
    private LocalDateTime taskStartTime;

    /** 当前打扫/维修信息（非打扫中/维修中时为 null） */
    @Schema(description = "当前打扫/维修信息")
    private TaskInfo currentTask;
}
