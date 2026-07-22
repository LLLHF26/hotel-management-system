package com.lhf.hotel.room.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class RoomTypeSaveDTO {

    @Schema(description = "房型名称")
    @NotBlank(message = "房型名称不能为空")
    private String name;

    @Schema(description = "价格")
    @NotNull(message = "价格不能为空")
    private BigDecimal price;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "面积")
    private Integer area;

    @Schema(description = "床型")
    private String bedType;

    @Schema(description = "最大入住人数")
    private Integer maxGuests;

    @Schema(description = "封面图片")
    private String coverImage;

    @Schema(description = "图片列表")
    private List<String> images;

    @Schema(description = "设施")
    private String amenities;

    @Schema(description = "排序顺序")
    private Integer sortOrder;
}
