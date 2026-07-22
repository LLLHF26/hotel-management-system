package com.lhf.hotel.system.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统设置展示 VO。
 */
@Data
public class SettingVO {

    private String key;
    private String value;
    private String description;
    private LocalDateTime updateTime;
}
