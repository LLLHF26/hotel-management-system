package com.lhf.hotel.room.controller;

import com.lhf.hotel.common.result.PageResult;
import com.lhf.hotel.common.result.Result;
import com.lhf.hotel.room.model.dto.MaintenanceCompleteDTO;
import com.lhf.hotel.room.model.dto.MaintenanceStartDTO;
import com.lhf.hotel.room.model.vo.MaintenanceRecordVO;
import com.lhf.hotel.room.service.MaintenanceService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "维修管理", description = "维修起止、维修记录查询")
@RestController
@RequestMapping("/api/room")
public class MaintenanceController {

    private final MaintenanceService service;

    public MaintenanceController(MaintenanceService service) {
        this.service = service;
    }

    /** 设为维修中（ADMIN） */
    @Operation(summary = "设为维修中")
    @PostMapping("/{id}/maintenance/start")
    public Result<?> start(@PathVariable @Parameter(description = "房间ID") Long id, @Valid @RequestBody @Parameter(description = "维修开始信息") MaintenanceStartDTO dto) {
        Long recordId = service.start(id, dto);
        return Result.ok("已设为维修中", java.util.Map.of("recordId", recordId));
    }

    /** 维修完成（ADMIN） */
    @Operation(summary = "维修完成")
    @PostMapping("/{id}/maintenance/complete")
    public Result<Void> complete(@PathVariable @Parameter(description = "房间ID") Long id, @Valid @RequestBody @Parameter(description = "维修完成信息") MaintenanceCompleteDTO dto) {
        service.complete(id, dto);
        return Result.ok("维修完成，房间已恢复空闲", null);
    }

    /** 维修记录列表 */
    @Operation(summary = "维修记录列表")
    @GetMapping("/maintenance/records")
    public Result<PageResult<MaintenanceRecordVO>> records(
            @RequestParam(defaultValue = "1") @Parameter(description = "页码") int page,
            @RequestParam(defaultValue = "10") @Parameter(description = "每页数量") int size,
            @RequestParam(required = false) @Parameter(description = "房间ID") Long roomId,
            @RequestParam(required = false) @Parameter(description = "维修状态") String status,
            @RequestParam(required = false) @Parameter(description = "开始日期") String startDate,
            @RequestParam(required = false) @Parameter(description = "结束日期") String endDate) {
        return Result.ok(service.records(page, size, roomId, status, startDate, endDate));
    }
}
