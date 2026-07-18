package com.lhf.hotel.order.controller;

import com.lhf.hotel.common.result.PageResult;
import com.lhf.hotel.common.result.Result;
import com.lhf.hotel.order.model.dto.*;
import com.lhf.hotel.order.model.vo.HotRoomTypeCountVO;
import com.lhf.hotel.common.model.vo.OrderVO;
import com.lhf.hotel.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping("/list")
    public Result<PageResult<OrderVO>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String customerPhone,
            @RequestParam(required = false) String roomNumber,
            @RequestParam(required = false) String checkInDate,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        PageResult<OrderVO> result = orderService.list(page, size, status, customerPhone,
                roomNumber, checkInDate, source, startDate, endDate);
        return Result.ok(result);
    }

    @GetMapping("/list/byTime")
    public Result<List<OrderVO>> listByTime(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        List<OrderVO> result = orderService.listByTime(startDate, endDate);
        return Result.ok(result);
    }

    @GetMapping("/{id}")
    public Result<OrderVO> getById(@PathVariable Long id) {
        return Result.ok(orderService.getById(id));
    }

    @PostMapping("/create")
    public Result<Map<String, Object>> create(@Valid @RequestBody OrderCreateDTO dto) {
        return Result.ok("预订成功", orderService.create(dto));
    }

    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id, @Valid @RequestBody OrderCancelDTO dto) {
        orderService.cancel(id, dto);
        return Result.ok("订单已取消", null);
    }

    @PutMapping("/{id}/check-in")
    public Result<Void> checkIn(@PathVariable Long id, @Valid @RequestBody CheckInDTO dto) {
        orderService.checkIn(id, dto);
        return Result.ok("入住办理成功", null);
    }

    @PutMapping("/{id}/checkout")
    public Result<Void> checkout(@PathVariable Long id, @Valid @RequestBody CheckOutDTO dto) {
        orderService.checkout(id, dto);
        return Result.ok("退房成功", null);
    }

    @PutMapping("/{id}/extend")
    public Result<Map<String, Object>> extend(@PathVariable Long id, @Valid @RequestBody ExtendDTO dto) {
        return Result.ok("续住成功", orderService.extend(id, dto));
    }

    @PutMapping("/{id}/change-room")
    public Result<Map<String, Object>> changeRoom(@PathVariable Long id, @Valid @RequestBody ChangeRoomDTO dto) {
        return Result.ok("换房成功", orderService.changeRoom(id, dto));
    }

    @GetMapping("/available-rooms")
    public Result<Map<String, Object>> getAvailableRooms(
            @RequestParam Long roomTypeId,
            @RequestParam String checkIn,
            @RequestParam String checkOut) {
        return Result.ok(orderService.getAvailableRooms(roomTypeId, checkIn, checkOut));
    }

    @GetMapping("/stats/hot-room-types")
    public Result<List<HotRoomTypeCountVO>> getHotRoomTypes(
            @RequestParam(defaultValue = "6") int topN,
            @RequestParam(defaultValue = "30") int days) {
        return Result.ok(orderService.getHotRoomTypeCounts(topN, days));
    }
}
