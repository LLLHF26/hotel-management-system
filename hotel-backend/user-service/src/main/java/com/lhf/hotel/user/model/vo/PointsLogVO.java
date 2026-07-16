package com.lhf.hotel.user.model.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PointsLogVO {

    private Long id;
    private String type;
    private String typeName;
    private Integer points;
    private Integer balanceAfter;
    private String reason;
    private LocalDateTime createTime;
}
