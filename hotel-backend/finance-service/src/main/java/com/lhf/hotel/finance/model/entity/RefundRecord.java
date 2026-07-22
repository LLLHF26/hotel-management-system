package com.lhf.hotel.finance.model.entity;

import com.alibaba.excel.annotation.ExcelIgnoreUnannotated;
import com.alibaba.excel.annotation.ExcelProperty;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("refund_record")
@ExcelIgnoreUnannotated
public class RefundRecord {

    @TableId(type = IdType.ASSIGN_ID)
    @ExcelProperty(value = "退款ID", index = 0)
    private Long id;

    @ExcelProperty(value = "订单ID", index = 1)
    private Long orderId;

    @ExcelProperty(value = "订单编号", index = 2)
    private String orderNo;

    @ExcelProperty(value = "支付ID", index = 3)
    private Long paymentId;

    @ExcelProperty(value = "退款金额", index = 4)
    private BigDecimal refundAmount;

    @ExcelProperty(value = "退款原因", index = 5)
    private String reason;

    @ExcelProperty(value = "退款状态", index = 6)
    private String status;

    @ExcelProperty(value = "操作人ID", index = 7)
    private Long operatorId;

    @TableField(fill = FieldFill.INSERT)
    @ExcelProperty(value = "创建时间", index = 8)
    private LocalDateTime createTime;
}
