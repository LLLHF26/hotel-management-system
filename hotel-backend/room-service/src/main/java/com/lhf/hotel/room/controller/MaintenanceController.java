package com.lhf.hotel.room.controller;

import com.lhf.hotel.common.result.PageResult;
import com.lhf.hotel.common.result.Result;
import com.lhf.hotel.room.model.dto.MaintenanceCompleteDTO;
import com.lhf.hotel.room.model.dto.MaintenanceStartDTO;
import com.lhf.hotel.room.model.vo.MaintenanceRecordVO;
import com.lhf.hotel.room.service.MaintenanceService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/room")
public class MaintenanceController {

    private final MaintenanceService service;

    public MaintenanceController(MaintenanceService service) {
        this.service = service;
    }

    /** 设为维修中（ADMIN） */
    @PostMapping("/{id}/maintenance/start")
    public Result<?> start(@PathVariable Long id, @Valid @RequestBody MaintenanceStartDTO dto) {
        Long recordId = service.start(id, dto);
        return Result.ok("已设为维修中", java.util.Map.of("recordId", recordId));
    }

    /** 维修完成（ADMIN） */
    @PostMapping("/{id}/maintenance/complete")
    public Result<Void> complete(@PathVariable Long id, @Valid @RequestBody MaintenanceCompleteDTO dto) {
        service.complete(id, dto);
        return Result.ok("维修完成，房间已恢复空闲", null);
    }

    /** 维修记录列表 */
    @GetMapping("/maintenance/records")
    public Result<PageResult<MaintenanceRecordVO>> records(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        return Result.ok(service.records(page, size, roomId, status, startDate, endDate));
    }
}
