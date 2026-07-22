package com.lhf.hotel.order.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductDTO {

    @Schema(description = "分类：食物 / 饮用水 / 日用品 / 其他")
    private String category;

    @Schema(description = "商品名称")
    @NotBlank(message = "商品名称不能为空")
    private String name;

    @Schema(description = "单价")
    @NotNull(message = "单价不能为空")
    @DecimalMin(value = "0.00", inclusive = false, message = "单价必须大于0")
    private BigDecimal price;

    @Schema(description = "封面图")
    private String image;

    @Schema(description = "计价单位：份 / 瓶 / 个 / 盒")
    private String unit;

    @Schema(description = "库存（-1 表示不限量）")
    private Integer stock = -1;

    @Schema(description = "状态：上架 / 下架")
    private String status = "上架";

    @Schema(description = "描述")
    private String description;

    @Schema(description = "排序（越小越靠前）")
    private Integer sortOrder = 0;
}
