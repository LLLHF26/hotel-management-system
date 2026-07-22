package com.lhf.hotel.room.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CleaningAssignDTO {

    @Schema(description = "保洁员ID")
    @NotNull(message = "保洁员不能为空")
    private Long cleanerId;
}
