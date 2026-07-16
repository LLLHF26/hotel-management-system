package com.lhf.hotel.common.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 房间状态变更请求
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomStatusChangeDTO {

    @NotNull(message = "目标状态不能为空")
    private String status;

    /** 变更原因（设为维修中时必填） */
    private String reason;
}
