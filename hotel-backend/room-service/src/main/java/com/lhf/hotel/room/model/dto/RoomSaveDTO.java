package com.lhf.hotel.room.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RoomSaveDTO {

    @NotBlank(message = "房间编号不能为空")
    private String roomNumber;

    @NotNull(message = "房型不能为空")
    private Long roomTypeId;

    private Integer floor;
    private String description;
}
