package com.lhf.hotel.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 保洁员信息（user-service → room-service）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CleanerDTO {

    private Long id;
    private String realName;
    private String phone;
    private int currentTaskCount;
}
