package com.lhf.hotel.room.model.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class MaintenanceRecordVO {

    private Long id;
    private Long roomId;
    private String roomNumber;
    private String reason;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private BigDecimal cost;
}
