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
public class PointsLogVO {

    @Schema(description = "日志ID")
    private Long id;

    @Schema(description = "类型")
    private String type;

    @Schema(description = "类型名称")
    private String typeName;

    @Schema(description = "积分")
    private Integer points;

    @Schema(description = "变更后余额")
    private Integer balanceAfter;

    @Schema(description = "原因")
    private String reason;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
