package com.lhf.hotel.order.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ProductOrderDTO {

    @Schema(description = "商品明细")
    @NotEmpty(message = "商品明细不能为空")
    private List<Item> items;

    @Schema(description = "使用的积分（抵扣），默认 0")
    @Min(value = 0, message = "积分不能为负")
    private Integer pointsUsed = 0;

    @Schema(description = "备注")
    private String remark;

    @Data
    public static class Item {
        @Schema(description = "商品ID")
        @NotNull(message = "商品ID不能为空")
        private Long productId;

        @Schema(description = "数量")
        @Min(value = 1, message = "数量至少 1")
        private Integer quantity = 1;
    }
}
