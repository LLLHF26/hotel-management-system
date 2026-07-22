package com.lhf.hotel.room.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomTypeVO {

    @Schema(description = "房型ID")
    private Long id;

    @Schema(description = "房型名称")
    private String name;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "面积")
    private Integer area;

    @Schema(description = "床型")
    private String bedType;

    @Schema(description = "最大入住人数")
    private Integer maxGuests;

    @Schema(description = "价格")
    private BigDecimal price;

    @Schema(description = "封面图片")
    private String coverImage;

    @Schema(description = "图片列表")
    private List<String> images;

    @Schema(description = "设施")
    private String amenities;

    @Schema(description = "排序顺序")
    private Integer sortOrder;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
