package com.lhf.hotel.finance.model.vo;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class OccupancyTrendVO {

    private List<String> dates;
    private List<String> occupancyRates;
    private Integer availableRooms;
    private String avgOccupancyRate;
}
