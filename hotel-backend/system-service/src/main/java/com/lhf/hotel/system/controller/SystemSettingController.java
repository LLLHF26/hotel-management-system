package com.lhf.hotel.system.controller;

import com.lhf.hotel.common.result.Result;
import com.lhf.hotel.system.model.dto.SettingUpdateDTO;
import com.lhf.hotel.system.model.vo.SettingVO;
import com.lhf.hotel.system.service.SystemSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 系统设置接口。
 * 路由前缀 /api/system，经 gateway 转发到 system-service。
 */
@Tag(name = "系统设置", description = "酒店基础信息与业务参数配置")
@RestController
@RequestMapping("/api/system/settings")
public class SystemSettingController {

    private final SystemSettingService settingService;

    public SystemSettingController(SystemSettingService settingService) {
        this.settingService = settingService;
    }

    @Operation(summary = "获取全部系统设置")
    @GetMapping
    public Result<List<SettingVO>> list() {
        return Result.ok(settingService.listAll());
    }

    @Operation(summary = "批量更新系统设置")
    @PutMapping
    public Result<List<SettingVO>> update(@Valid @RequestBody List<SettingUpdateDTO> items) {
        return Result.ok("保存成功", settingService.updateBatch(items));
    }
}
