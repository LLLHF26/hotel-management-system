package com.lhf.hotel.room.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class RoomTypeSaveDTO {

    @NotBlank(message = "房型名称不能为空")
    private String name;

    @NotNull(message = "价格不能为空")
    private BigDecimal price;

    private String description;
    private Integer area;
    private String bedType;
    private Integer maxGuests;
    private String coverImage;
    private List<String> images;
    private String amenities;
    private Integer sortOrder;
}
