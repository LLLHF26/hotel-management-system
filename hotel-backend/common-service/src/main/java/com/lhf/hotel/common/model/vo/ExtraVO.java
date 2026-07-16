package com.lhf.hotel.common.model.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class ExtraVO {

    private Long id;
    private String itemName;
    private BigDecimal amount;
    private Integer quantity;
    private BigDecimal subtotal;
    private Long operatorId;
    private String operatorName;
    private LocalDateTime createTime;
}
