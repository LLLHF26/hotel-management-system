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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "房间管理", description = "房间CRUD、房态看板、房态变更、保洁分配")
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
    @Operation(summary = "房间列表")
    @GetMapping("/list")
    public Result<PageResult<RoomVO>> list(
            @RequestParam(defaultValue = "1") @Parameter(description = "页码") int page,
            @RequestParam(defaultValue = "10") @Parameter(description = "每页数量") int size,
            @RequestParam(required = false) @Parameter(description = "房型ID") Long roomTypeId,
            @RequestParam(required = false) @Parameter(description = "房间状态") String status,
            @RequestParam(required = false) @Parameter(description = "楼层") Integer floor,
            @RequestParam(required = false) @Parameter(description = "搜索关键词") String keyword) {
        return Result.ok(roomService.list(page, size, roomTypeId, status, floor, keyword));
    }

    /** 房间详情（含打扫/维修进度） */
    @Operation(summary = "房间详情（含打扫/维修进度）")
    @GetMapping("/{id}")
    public Result<RoomVO> detail(@PathVariable @Parameter(description = "房间ID") Long id) {
        return Result.ok(roomService.getById(id));
    }

    /** 创建房间（ADMIN） */
    @Operation(summary = "创建房间")
    @PostMapping("/create")
    public Result<?> create(@Valid @RequestBody @Parameter(description = "房间信息") RoomSaveDTO dto) {
        Long id = roomService.create(dto);
        return Result.ok("创建成功", java.util.Map.of("id", id));
    }

    /** 修改房间（ADMIN） */
    @Operation(summary = "修改房间")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable @Parameter(description = "房间ID") Long id, @RequestBody @Parameter(description = "房间信息") RoomSaveDTO dto) {
        roomService.update(id, dto);
        return Result.ok("修改成功", null);
    }

    /** 删除房间（ADMIN） */
    @Operation(summary = "删除房间")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable @Parameter(description = "房间ID") Long id) {
        roomService.delete(id);
        return Result.ok("删除成功", null);
    }

    // ==================== 房态 ====================

    /** 房态看板（前台核心接口） */
    @Operation(summary = "房态看板")
    @GetMapping("/dashboard")
    public Result<DashboardVO> dashboard(@RequestParam(required = false) @Parameter(description = "楼层") Integer floor) {
        return Result.ok(roomService.dashboard(floor));
    }

    /** 按状态筛选房间 */
    @Operation(summary = "按状态筛选房间")
    @GetMapping("/status/{status}")
    public Result<PageResult<RoomVO>> listByStatus(
            @PathVariable @Parameter(description = "房间状态") String status,
            @RequestParam(defaultValue = "1") @Parameter(description = "页码") int page,
            @RequestParam(defaultValue = "20") @Parameter(description = "每页数量") int size) {
        return Result.ok(roomService.listByStatus(status, page, size));
    }

    /** 手动变更房态（含合法跳转校验） */
    @Operation(summary = "手动变更房态")
    @PutMapping("/{id}/status")
    public Result<Void> changeStatus(@PathVariable @Parameter(description = "房间ID") Long id,
                                      @Valid @RequestBody @Parameter(description = "房态变更信息") RoomStatusChangeDTO dto) {
        roomService.changeStatus(id, dto);
        return Result.ok("房态已变更为 " + dto.getStatus(), null);
    }

    /** 手动分配保洁 */
    @Operation(summary = "手动分配保洁")
    @PostMapping("/{id}/cleaning/assign")
    public Result<?> assignCleaning(@PathVariable @Parameter(description = "房间ID") Long id, @Valid @RequestBody @Parameter(description = "保洁分配信息") CleaningAssignDTO dto) {
        CleaningAssignResult result = cleaningService.assign(id, dto);
        return Result.ok("已分配" + result.cleanerName(), java.util.Map.of("taskId", result.taskId()));
    }
}
