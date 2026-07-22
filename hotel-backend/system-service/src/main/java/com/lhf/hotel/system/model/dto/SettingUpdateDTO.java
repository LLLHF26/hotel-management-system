package com.lhf.hotel.system.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 系统设置更新 DTO。
 */
@Data
public class SettingUpdateDTO {

    @NotBlank(message = "配置键不能为空")
    @Size(max = 64, message = "配置键长度不能超过 64")
    private String key;

    @Size(max = 4096, message = "配置值长度不能超过 4096")
    private String value;
}
