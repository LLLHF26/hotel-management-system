package com.lhf.hotel.room.controller;

import com.lhf.hotel.common.dto.RoomStatusChangeDTO;
import com.lhf.hotel.common.result.PageResult;
import com.lhf.hotel.common.result.Result;
import com.lhf.hotel.room.model.dto.CleaningAssignDTO;
import com.lhf.hotel.room.model.dto.RoomSaveDTO;
import com.lhf.hotel.room.model.vo.DashboardVO;
import com.lhf.hotel.room.model.vo.RoomVO;
import com.lhf.hotel.room.service.CleaningService;
import com.lhf.hotel.room.service.RoomService;
import com.lhf.hotel.room.model.vo.CleaningAssignResult;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/room")
public class RoomController {

    private final RoomService roomService;
    private final CleaningService cleaningService;

    public RoomController(RoomService roomService, CleaningService cleaningService) {
        this.roomService = roomService;
        this.cleaningService = cleaningService;
    }

    // ==================== 房间 CRUD ====================

    /** 房间列表 */
    @GetMapping("/list")
    public Result<PageResult<RoomVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long roomTypeId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer floor,
            @RequestParam(required = false) String keyword) {
        return Result.ok(roomService.list(page, size, roomTypeId, status, floor, keyword));
    }

    /** 房间详情（含打扫/维修进度） */
    @GetMapping("/{id}")
    public Result<RoomVO> detail(@PathVariable Long id) {
        return Result.ok(roomService.getById(id));
    }

    /** 创建房间（ADMIN） */
    @PostMapping("/create")
    public Result<?> create(@Valid @RequestBody RoomSaveDTO dto) {
        Long id = roomService.create(dto);
        return Result.ok("创建成功", java.util.Map.of("id", id));
    }

    /** 修改房间（ADMIN） */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody RoomSaveDTO dto) {
        roomService.update(id, dto);
        return Result.ok("修改成功", null);
    }

    /** 删除房间（ADMIN） */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        roomService.delete(id);
        return Result.ok("删除成功", null);
    }

    // ==================== 房态 ====================

    /** 房态看板（前台核心接口） */
    @GetMapping("/dashboard")
    public Result<DashboardVO> dashboard(@RequestParam(required = false) Integer floor) {
        return Result.ok(roomService.dashboard(floor));
    }

    /** 按状态筛选房间 */
    @GetMapping("/status/{status}")
    public Result<PageResult<RoomVO>> listByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.ok(roomService.listByStatus(status, page, size));
    }

    /** 手动变更房态（含合法跳转校验） */
    @PutMapping("/{id}/status")
    public Result<Void> changeStatus(@PathVariable Long id,
                                      @Valid @RequestBody RoomStatusChangeDTO dto) {
        roomService.changeStatus(id, dto);
        return Result.ok("房态已变更为 " + dto.getStatus(), null);
    }

    /** 手动分配保洁 */
    @PostMapping("/{id}/cleaning/assign")
    public Result<?> assignCleaning(@PathVariable Long id, @Valid @RequestBody CleaningAssignDTO dto) {
        CleaningAssignResult result = cleaningService.assign(id, dto);
        return Result.ok("已分配" + result.cleanerName(), java.util.Map.of("taskId", result.taskId()));
    }
}
