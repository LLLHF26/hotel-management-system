package com.lhf.hotel.room.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

/** 房态看板 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardVO {

    /** 总数 */
    @Schema(description = "房间总数")
    private Integer total;

    /** 各状态数量 {"空闲中":20, "预订中":15, ...} */
    @Schema(description = "各状态数量统计")
    private Map<String, Integer> summary;

    /** 全部房间列表（前端按 status 分组渲染） */
    @Schema(description = "房间列表")
    private List<RoomVO> rooms;
}
