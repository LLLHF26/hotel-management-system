package com.lhf.hotel.order.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChangeRoomDTO {

    @Schema(description = "新房间ID")
    @NotNull(message = "新房间不能为空")
    private Long newRoomId;

    @Schema(description = "换房起始日期(yyyy-MM-dd)，不填则默认从订单入住日期开始")
    private String startDate;

    @Schema(description = "换房结束日期(yyyy-MM-dd)，不填则到订单退房日期")
    private String endDate;
}
