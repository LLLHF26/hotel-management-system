package com.lhf.hotel.common.model.vo;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@ExcelIgnoreUnannotated
public class OrderVO {
    @ExcelProperty(value = "订单ID", index = 0)
    private Long id;
    @ExcelProperty(value = "订单编号", index = 1)
    private String orderNo;
    private Long customerId;
    @ExcelProperty(value = "客户姓名", index = 2)
    private String customerName;
    @ExcelProperty(value = "客户电话", index = 3)
    private String customerPhone;
    private Long roomId;
    @ExcelProperty(value = "房间号", index = 4)
    private String roomNumber;
    @ExcelProperty(value = "房型名称", index = 5)
    private String roomTypeName;
    @ExcelProperty(value = "入住日期", index = 6)
    private LocalDate checkInDate;
    @ExcelProperty(value = "离店日期", index = 7)
    private LocalDate checkOutDate;
    @ExcelProperty(value = "入住晚数", index = 8)
    private Integer nights;
    private LocalDateTime actualCheckIn;
    private LocalDateTime actualCheckOut;
    private BigDecimal roomPrice;
    private BigDecimal roomTotal;
    private BigDecimal extraTotal;
    @ExcelProperty(value = "订单总价", index = 9)
    private BigDecimal totalAmount;
    @ExcelProperty(value = "已支付金额", index = 10)
    private BigDecimal paidAmount;
    private BigDecimal deposit;
    private String status;
    @ExcelProperty(value = "订单状态", index = 11)
    private String statusName;
    private String source;
    private String sourceName;
    private String remark;
    @ExcelProperty(value = "创建时间", index = 12)
    private LocalDateTime createTime;

    /** 消费明细 */
    private List<ExtraVO> extras;

    /** 支付记录 */
    private List<PaymentVO> payments;
}
