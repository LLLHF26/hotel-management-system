package com.lhf.hotel.room.model.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class RoomTypeVO {

    private Long id;
    private String name;
    private String description;
    private Integer area;
    private String bedType;
    private Integer maxGuests;
    private BigDecimal price;
    private String coverImage;
    private List<String> images;
    private String amenities;
    private Integer sortOrder;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
