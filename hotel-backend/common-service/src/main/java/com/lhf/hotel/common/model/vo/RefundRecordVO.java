package com.lhf.hotel.common.model.vo;

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
public class RefundRecordVO {

    @Schema(description = "退款记录ID")
    private Long id;

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "订单编号")
    private String orderNo;

    @Schema(description = "支付编号")
    private String paymentNo;

    @Schema(description = "退款金额")
    private BigDecimal refundAmount;

    @Schema(description = "退款原因")
    private String reason;

    @Schema(description = "退款状态")
    private String status;

    @Schema(description = "操作员姓名")
    private String operatorName;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
