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

import java.util.List;

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
    @GetMapping("/list")
    public Result<PageResult<RoomTypeVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String bedType) {
        return Result.ok(service.list(page, size, keyword, bedType));
    }

    /** 房型详情 */
    @GetMapping("/{id}")
    public Result<RoomTypeVO> detail(@PathVariable Long id) {
        return Result.ok(service.getById(id));
    }

    /** 创建房型（ADMIN） */
    @PostMapping("/create")
    public Result<?> create(@Valid @RequestBody RoomTypeSaveDTO dto) {
        Long id = service.create(dto);
        return Result.ok("创建成功", java.util.Map.of("id", id));
    }

    /** 修改房型（ADMIN） */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody RoomTypeSaveDTO dto) {
        service.update(id, dto);
        return Result.ok("修改成功", null);
    }

    /** 删除房型（ADMIN） */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return Result.ok("删除成功", null);
    }

    /** 热门房型（客户端免登录，按近30天订单量排名） */
    @GetMapping("/hot")
    public Result<List<HotRoomTypeVO>> hot() {
        return Result.ok(hotService.getHotRoomTypes());
    }
}
