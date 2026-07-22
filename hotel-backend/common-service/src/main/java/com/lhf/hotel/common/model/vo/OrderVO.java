package com.lhf.hotel.common.model.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ExcelIgnoreUnannotated
public class OrderVO {

    @Schema(description = "订单ID")
    @ExcelProperty(value = "订单ID", index = 0)
    private Long id;

    @Schema(description = "订单编号")
    @ExcelProperty(value = "订单编号", index = 1)
    private String orderNo;

    @Schema(description = "客户ID")
    private Long customerId;

    @Schema(description = "客户姓名")
    @ExcelProperty(value = "客户姓名", index = 2)
    private String customerName;

    @Schema(description = "客户电话")
    @ExcelProperty(value = "客户电话", index = 3)
    private String customerPhone;

    @Schema(description = "房间ID")
    private Long roomId;

    @Schema(description = "房间号")
    @ExcelProperty(value = "房间号", index = 4)
    private String roomNumber;

    @Schema(description = "房型名称")
    @ExcelProperty(value = "房型名称", index = 5)
    private String roomTypeName;

    @Schema(description = "入住日期")
    @ExcelProperty(value = "入住日期", index = 6)
    private LocalDate checkInDate;

    @Schema(description = "离店日期")
    @ExcelProperty(value = "离店日期", index = 7)
    private LocalDate checkOutDate;

    @Schema(description = "入住晚数")
    @ExcelProperty(value = "入住晚数", index = 8)
    private Integer nights;

    @Schema(description = "实际入住时间")
    private LocalDateTime actualCheckIn;

    @Schema(description = "实际离店时间")
    private LocalDateTime actualCheckOut;

    @Schema(description = "房间单价")
    private BigDecimal roomPrice;

    @Schema(description = "房费合计")
    private BigDecimal roomTotal;

    @Schema(description = "额外消费合计")
    private BigDecimal extraTotal;

    @Schema(description = "订单总价")
    @ExcelProperty(value = "订单总价", index = 9)
    private BigDecimal totalAmount;

    @Schema(description = "已支付金额")
    @ExcelProperty(value = "已支付金额", index = 10)
    private BigDecimal paidAmount;

    @Schema(description = "押金")
    private BigDecimal deposit;

    @Schema(description = "状态编码")
    private String status;

    @Schema(description = "订单状态")
    @ExcelProperty(value = "订单状态", index = 11)
    private String statusName;

    @Schema(description = "来源编码")
    private String source;

    @Schema(description = "来源名称")
    private String sourceName;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    @ExcelProperty(value = "创建时间", index = 12)
    private LocalDateTime createTime;

    /** 消费明细 */
    @Schema(description = "消费明细")
    private List<ExtraVO> extras;

    /** 支付记录 */
    @Schema(description = "支付记录")
    private List<PaymentVO> payments;
}
