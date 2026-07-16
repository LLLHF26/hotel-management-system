package com.lhf.hotel.room.controller;

import com.lhf.hotel.common.result.PageResult;
import com.lhf.hotel.common.result.Result;
import com.lhf.hotel.room.model.vo.CleaningTaskVO;
import com.lhf.hotel.room.service.CleaningService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/room/cleaning")
public class CleaningController {

    private final CleaningService service;

    public CleaningController(CleaningService service) {
        this.service = service;
    }

    /** 打扫任务列表 */
    @GetMapping("/tasks")
    public Result<PageResult<CleaningTaskVO>> listTasks(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long cleanerId,
            @RequestParam(required = false) String date) {
        return Result.ok(service.listTasks(page, size, status, cleanerId, date));
    }

    /** 打扫任务详情 */
    @GetMapping("/task/{id}")
    public Result<CleaningTaskVO> taskDetail(@PathVariable Long id) {
        return Result.ok(service.getTaskById(id));
    }

    /** 保洁员当前任务 */
    @GetMapping("/tasks/cleaner/{cleanerId}/active")
    public Result<List<CleaningTaskVO>> activeTasks(@PathVariable Long cleanerId) {
        return Result.ok(service.getActiveTasksByCleaner(cleanerId));
    }
}
