package com.lhf.hotel.finance.model.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
@ExcelIgnoreUnannotated
public class RoomTypeRevenueVO {


    @ExcelProperty(value = "房型ID", index = 0)
    private Long roomTypeId;

    @ExcelProperty(value = "房型名称", index = 1)
    private String roomTypeName;

    @ExcelProperty(value = "订单数量", index = 2)
    private Integer orderCount;

    @ExcelProperty(value = "总营收", index = 3)
    private BigDecimal revenue;

    @ExcelProperty(value = "房间数量", index = 4)
    private Integer roomCount;

    @ExcelProperty(value = "入住率", index = 5)
    private String occupancyRate;

    private Integer rank;
}
