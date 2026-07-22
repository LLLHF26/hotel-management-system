package com.lhf.hotel.room.controller;

import com.lhf.hotel.common.result.PageResult;
import com.lhf.hotel.common.result.Result;
import com.lhf.hotel.room.model.vo.CleaningTaskVO;
import com.lhf.hotel.room.service.CleaningService;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "打扫管理", description = "打扫任务列表、保洁员任务")
@RestController
@RequestMapping("/api/room/cleaning")
public class CleaningController {

    private final CleaningService service;

    public CleaningController(CleaningService service) {
        this.service = service;
    }

    /** 打扫任务列表 */
    @Operation(summary = "打扫任务列表")
    @GetMapping("/tasks")
    public Result<PageResult<CleaningTaskVO>> listTasks(
            @RequestParam(defaultValue = "1") @Parameter(description = "页码") int page,
            @RequestParam(defaultValue = "10") @Parameter(description = "每页数量") int size,
            @RequestParam(required = false) @Parameter(description = "任务状态") String status,
            @RequestParam(required = false) @Parameter(description = "保洁员ID") Long cleanerId,
            @RequestParam(required = false) @Parameter(description = "日期") String date) {
        return Result.ok(service.listTasks(page, size, status, cleanerId, date));
    }

    /** 打扫任务详情 */
    @Operation(summary = "打扫任务详情")
    @GetMapping("/task/{id}")
    public Result<CleaningTaskVO> taskDetail(@PathVariable @Parameter(description = "任务ID") Long id) {
        return Result.ok(service.getTaskById(id));
    }

    /** 保洁员当前任务 */
    @Operation(summary = "保洁员当前任务")
    @GetMapping("/tasks/cleaner/{cleanerId}/active")
    public Result<List<CleaningTaskVO>> activeTasks(@PathVariable @Parameter(description = "保洁员ID") Long cleanerId) {
        return Result.ok(service.getActiveTasksByCleaner(cleanerId));
    }
}
