package com.lhf.hotel.order.model.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderCreateDTO {

    @NotNull(message = "房型不能为空")
    private Long roomTypeId;

    private Long customerId;

    @NotNull(message = "入住日期不能为空")
    private String checkInDate;

    @NotNull(message = "退房日期不能为空")
    private String checkOutDate;

    private Integer roomCount;

    private String guestName;

    private String guestPhone;

    private String remark;
}
