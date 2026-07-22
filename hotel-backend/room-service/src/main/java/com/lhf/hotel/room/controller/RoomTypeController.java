package com.lhf.hotel.room.controller;

import com.lhf.hotel.common.result.PageResult;
import com.lhf.hotel.common.result.Result;
import com.lhf.hotel.room.model.dto.RoomTypeSaveDTO;
import com.lhf.hotel.room.model.vo.HotRoomTypeVO;
import com.lhf.hotel.room.model.vo.RoomTypeVO;
import com.lhf.hotel.room.service.RoomTypeHotService;
import com.lhf.hotel.room.service.RoomTypeService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "房型管理", description = "房型CRUD、热门房型")
@RestController
@RequestMapping("/api/room/type")
public class RoomTypeController {

    private final RoomTypeService service;
    private final RoomTypeHotService hotService;

    public RoomTypeController(RoomTypeService service, RoomTypeHotService hotService) {
        this.service = service;
        this.hotService = hotService;
    }

    /** 房型列表（客户端免登录） */
    @Operation(summary = "房型列表")
    @GetMapping("/list")
    public Result<PageResult<RoomTypeVO>> list(
            @RequestParam(defaultValue = "1") @Parameter(description = "页码") int page,
            @RequestParam(defaultValue = "10") @Parameter(description = "每页数量") int size,
            @RequestParam(required = false) @Parameter(description = "搜索关键词") String keyword,
            @RequestParam(required = false) @Parameter(description = "床型") String bedType) {
        return Result.ok(service.list(page, size, keyword, bedType));
    }

    /** 房型详情 */
    @Operation(summary = "房型详情")
    @GetMapping("/{id}")
    public Result<RoomTypeVO> detail(@PathVariable @Parameter(description = "房型ID") Long id) {
        return Result.ok(service.getById(id));
    }

    /** 创建房型（ADMIN） */
    @Operation(summary = "创建房型")
    @PostMapping("/create")
    public Result<?> create(@Valid @RequestBody @Parameter(description = "房型信息") RoomTypeSaveDTO dto) {
        Long id = service.create(dto);
        return Result.ok("创建成功", java.util.Map.of("id", id));
    }

    /** 修改房型（ADMIN） */
    @Operation(summary = "修改房型")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable @Parameter(description = "房型ID") Long id, @RequestBody @Parameter(description = "房型信息") RoomTypeSaveDTO dto) {
        service.update(id, dto);
        return Result.ok("修改成功", null);
    }

    /** 删除房型（ADMIN） */
    @Operation(summary = "删除房型")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable @Parameter(description = "房型ID") Long id) {
        service.delete(id);
        return Result.ok("删除成功", null);
    }

    /** 热门房型（客户端免登录，按近30天订单量排名） */
    @Operation(summary = "热门房型")
    @GetMapping("/hot")
    public Result<List<HotRoomTypeVO>> hot() {
        return Result.ok(hotService.getHotRoomTypes());
    }
}
