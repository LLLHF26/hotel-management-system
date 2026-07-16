package com.lhf.hotel.room.model.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

/** 房态看板 */
@Data
@Builder
public class DashboardVO {

    /** 总数 */
    private Integer total;

    /** 各状态数量 {"空闲中":20, "预订中":15, ...} */
    private Map<String, Integer> summary;

    /** 全部房间列表（前端按 status 分组渲染） */
    private List<RoomVO> rooms;
}
