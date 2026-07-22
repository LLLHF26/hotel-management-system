package com.lhf.hotel.order.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 客房商品订单（order_extra 记录 + 关联订单冗余信息），供前台配送提醒使用
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductOrderVO {

    private Long id;
    private Long orderId;
    private String itemName;
    private BigDecimal amount;
    private Integer quantity;
    private Long operatorId;
    private LocalDateTime createTime;

    // 冗余：关联订单信息，便于前台展示房间/客户
    private String orderNo;
    private String roomNumber;
    private String customerName;
}
