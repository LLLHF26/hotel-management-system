package com.lhf.hotel.room.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CleaningAssignDTO {

    @NotNull(message = "保洁员不能为空")
    private Long cleanerId;
}
