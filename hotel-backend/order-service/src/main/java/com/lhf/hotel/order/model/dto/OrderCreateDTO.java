package com.lhf.hotel.order.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderCreateDTO {

    @Schema(description = "房型ID")
    @NotNull(message = "房型不能为空")
    private Long roomTypeId;

    @Schema(description = "客户ID")
    private Long customerId;

    @Schema(description = "入住日期")
    @NotNull(message = "入住日期不能为空")
    private String checkInDate;

    @Schema(description = "退房日期")
    @NotNull(message = "退房日期不能为空")
    private String checkOutDate;

    @Schema(description = "房间数量")
    private Integer roomCount;

    @Schema(description = "住客姓名")
    private String guestName;

    @Schema(description = "住客电话")
    private String guestPhone;

    @Schema(description = "备注")
    private String remark;
}
