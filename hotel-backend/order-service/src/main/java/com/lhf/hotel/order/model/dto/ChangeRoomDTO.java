package com.lhf.hotel.order.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChangeRoomDTO {

    @NotNull(message = "新房间不能为空")
    private Long newRoomId;
}
