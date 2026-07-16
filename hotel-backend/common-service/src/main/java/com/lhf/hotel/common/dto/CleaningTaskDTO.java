package com.lhf.hotel.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 打扫任务摘要（跨 user / room 服务传输）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CleaningTaskDTO {

    private Long taskId;
    private Long roomId;
    private String roomNumber;
    private Long cleanerId;
    private String cleanerName;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
