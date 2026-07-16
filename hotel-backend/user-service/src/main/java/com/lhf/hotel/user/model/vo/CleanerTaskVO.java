package com.lhf.hotel.user.model.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CleanerTaskVO {

    private Long taskId;
    private Long roomId;
    private String roomNumber;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
