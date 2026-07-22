package com.lhf.hotel.finance.model.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ExcelIgnoreUnannotated
public class RoomTypeRevenueVO {

    @Schema(description = "房型ID")
    @ExcelProperty(value = "房型ID", index = 0)
    private Long roomTypeId;

    @Schema(description = "房型名称")
    @ExcelProperty(value = "房型名称", index = 1)
    private String roomTypeName;

    @Schema(description = "订单数量")
    @ExcelProperty(value = "订单数量", index = 2)
    private Integer orderCount;

    @Schema(description = "总营收")
    @ExcelProperty(value = "总营收", index = 3)
    private BigDecimal revenue;

    @Schema(description = "房间数量")
    @ExcelProperty(value = "房间数量", index = 4)
    private Integer roomCount;

    @Schema(description = "入住率")
    @ExcelProperty(value = "入住率", index = 5)
    private String occupancyRate;

    @Schema(description = "排名")
    private Integer rank;
}
