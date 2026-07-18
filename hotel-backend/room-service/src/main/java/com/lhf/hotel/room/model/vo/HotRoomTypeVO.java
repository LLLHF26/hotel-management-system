package com.lhf.hotel.room.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HotRoomTypeVO {
    private Long id;
    private String name;
    private String bedType;
    private Integer maxGuests;
    private BigDecimal price;
    private String coverImage;
    private String description;
    private Long orderCount;
}
