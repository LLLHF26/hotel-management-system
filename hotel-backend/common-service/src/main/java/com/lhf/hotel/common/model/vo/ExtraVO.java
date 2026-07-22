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
public class ExtraVO {

    @Schema(description = "消费明细ID")
    private Long id;

    @Schema(description = "项目名称")
    private String itemName;

    @Schema(description = "单价")
    private BigDecimal amount;

    @Schema(description = "数量")
    private Integer quantity;

    @Schema(description = "小计")
    private BigDecimal subtotal;

    @Schema(description = "操作员ID")
    private Long operatorId;

    @Schema(description = "操作员姓名")
    private String operatorName;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
