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
public class PaymentVO {

    @Schema(description = "支付记录ID")
    private Long id;

    @Schema(description = "支付编号")
    private String paymentNo;

    @Schema(description = "支付金额")
    private BigDecimal amount;

    @Schema(description = "支付方式编码")
    private String method;

    @Schema(description = "支付方式")
    private String methodName;

    @Schema(description = "支付状态")
    private String status;

    @Schema(description = "支付时间")
    private LocalDateTime paidAt;
}
