package com.lhf.hotel.order.model.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CheckOutDTO {

    private BigDecimal refundDeposit;
}
