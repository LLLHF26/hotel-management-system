package com.lhf.hotel.finance.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueDetailVO {

    @Schema(description = "支付编号")
    private String paymentNo;

    @Schema(description = "订单编号")
    private String orderNo;

    @Schema(description = "房间号")
    private String roomNumber;

    @Schema(description = "房型名称")
    private String roomTypeName;

    @Schema(description = "客户姓名")
    private String customerName;

    @Schema(description = "金额")
    private BigDecimal amount;

    @Schema(description = "支付方式代码")
    private String method;

    @Schema(description = "支付方式名称")
    private String methodName;

    @Schema(description = "类型（收入或退款）")
    private String type;

    @Schema(description = "支付时间")
    private LocalDateTime paidAt;
}
