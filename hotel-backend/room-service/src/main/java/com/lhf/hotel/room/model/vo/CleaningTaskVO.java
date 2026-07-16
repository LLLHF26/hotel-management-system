package com.lhf.hotel.room.model.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class CleaningTaskVO {

    private Long id;
    private Long roomId;
    private String roomNumber;
    private String roomTypeName;
    private Long cleanerId;
    private String cleanerName;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer durationMinutes;   // 打扫耗时（已完成时）
    private Integer elapsedMinutes;    // 已耗时（打扫中时）
}
