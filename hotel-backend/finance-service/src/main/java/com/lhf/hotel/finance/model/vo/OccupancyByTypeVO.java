package com.lhf.hotel.finance.model.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OccupancyByTypeVO {

    private String roomTypeName;
    private Integer totalRooms;
    private Integer occupiedNights;
    private Integer totalNights;
    private String occupancyRate;
}
