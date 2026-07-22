package com.lhf.hotel.finance.model.vo;

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
public class PaymentMethodStatVO {

    @Schema(description = "支付方式代码")
    private String method;

    @Schema(description = "支付方式名称")
    private String methodName;

    @Schema(description = "金额")
    private BigDecimal amount;

    @Schema(description = "笔数")
    private Integer count;

    @Schema(description = "占比")
    private String rate;
}
