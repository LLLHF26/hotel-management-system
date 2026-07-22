package com.lhf.hotel.order.controller;

import com.lhf.hotel.common.result.PageResult;
import com.lhf.hotel.common.result.Result;
import com.lhf.hotel.order.model.dto.*;
import com.lhf.hotel.order.model.vo.HotRoomTypeCountVO;
import com.lhf.hotel.common.model.vo.OrderVO;
import com.lhf.hotel.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "订单管理", description = "订单的创建、查询、取消、入住、退房、续住、换房")
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
@RefreshScope
public class OrderController {

    private final OrderService orderService;

    @Value("${hotel.config.message:default-from-local}")
    private String configMessage;

    /** Nacos 配置中心演示（动态刷新） */
    @Operation(summary = "Nacos配置中心演示（动态刷新）")
    @GetMapping("/config-demo")
    public Result<String> configDemo() {
        return Result.ok(configMessage);
    }

    /** 订单列表（多条件筛选） */
    @Operation(summary = "订单列表（多条件筛选）")
    @GetMapping("/list")
    public Result<PageResult<OrderVO>> list(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "订单状态") @RequestParam(required = false) String status,
            @Parameter(description = "客户手机号") @RequestParam(required = false) String customerPhone,
            @Parameter(description = "房间号") @RequestParam(required = false) String roomNumber,
            @Parameter(description = "入住日期") @RequestParam(required = false) String checkInDate,
            @Parameter(description = "订单来源") @RequestParam(required = false) String source,
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate) {
        PageResult<OrderVO> result = orderService.list(page, size, status, customerPhone,
                roomNumber, checkInDate, source, startDate, endDate);
        return Result.ok(result);
    }

    /** 按时间段查询订单列表 */
    @Operation(summary = "按时间段查询订单列表")
    @GetMapping("/list/byTime")
    public Result<List<OrderVO>> listByTime(
            @Parameter(description = "开始日期") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期") @RequestParam(required = false) String endDate) {
        List<OrderVO> result = orderService.listByTime(startDate, endDate);
        return Result.ok(result);
    }

    /** 订单详情 */
    @Operation(summary = "订单详情")
    @GetMapping("/{id}")
    public Result<OrderVO> getById(@Parameter(description = "订单ID") @PathVariable Long id) {
        return Result.ok(orderService.getById(id));
    }

    /** 创建订单（支持幂等性键防重复提交） */
    @Operation(summary = "创建订单（支持幂等性键防重复提交）")
    @PostMapping("/create")
    public Result<Map<String, Object>> create(
            @Parameter(description = "订单创建信息") @Valid @RequestBody OrderCreateDTO dto,
            @Parameter(description = "幂等性键，用于防重复提交", required = true) @RequestHeader(value = "X-Idempotency-Key", required = false) String idempotencyKey) {
        return Result.ok("预订成功", orderService.create(dto, idempotencyKey));
    }

    /** 取消订单 */
    @Operation(summary = "取消订单")
    @PutMapping("/{id}/cancel")
    public Result<Void> cancel(@Parameter(description = "订单ID") @PathVariable Long id, @Parameter(description = "取消信息") @Valid @RequestBody OrderCancelDTO dto) {
        orderService.cancel(id, dto);
        return Result.ok("订单已取消", null);
    }

    /** 办理入住 */
    @Operation(summary = "办理入住")
    @PutMapping("/{id}/check-in")
    public Result<Void> checkIn(@Parameter(description = "订单ID") @PathVariable Long id, @Parameter(description = "入住信息") @Valid @RequestBody CheckInDTO dto) {
        orderService.checkIn(id, dto);
        return Result.ok("入住办理成功", null);
    }

    /** 办理退房 */
    @Operation(summary = "办理退房")
    @PutMapping("/{id}/checkout")
    public Result<Void> checkout(@Parameter(description = "订单ID") @PathVariable Long id, @Parameter(description = "退房信息") @Valid @RequestBody CheckOutDTO dto) {
        orderService.checkout(id, dto);
        return Result.ok("退房成功", null);
    }

    /** 续住 */
    @Operation(summary = "续住")
    @PutMapping("/{id}/extend")
    public Result<Map<String, Object>> extend(@Parameter(description = "订单ID") @PathVariable Long id, @Parameter(description = "续住信息") @Valid @RequestBody ExtendDTO dto) {
        return Result.ok("续住成功", orderService.extend(id, dto));
    }

    /** 客房商品下单（含积分抵扣） */
    @Operation(summary = "客房商品下单（含积分抵扣）")
    @PostMapping("/{id}/product-order")
    public Result<Map<String, Object>> productOrder(
            @Parameter(description = "订单ID") @PathVariable Long id,
            @Parameter(description = "下单信息") @Valid @RequestBody ProductOrderDTO dto) {
        return Result.ok("下单成功", orderService.productOrder(id, dto));
    }

    /** 换房 */
    @Operation(summary = "换房")
    @PutMapping("/{id}/change-room")
    public Result<Map<String, Object>> changeRoom(@Parameter(description = "订单ID") @PathVariable Long id, @Parameter(description = "换房信息") @Valid @RequestBody ChangeRoomDTO dto) {
        return Result.ok("换房成功", orderService.changeRoom(id, dto));
    }

    /** 查询指定房型在时间段内的可用房间 */
    @Operation(summary = "查询指定房型在时间段内的可用房间")
    @GetMapping("/available-rooms")
    public Result<Map<String, Object>> getAvailableRooms(
            @Parameter(description = "房型ID") @RequestParam Long roomTypeId,
            @Parameter(description = "入住日期") @RequestParam String checkIn,
            @Parameter(description = "退房日期") @RequestParam String checkOut) {
        return Result.ok(orderService.getAvailableRooms(roomTypeId, checkIn, checkOut));
    }

    /** 热门房型统计（按订单量排名） */
    @Operation(summary = "热门房型统计（按订单量排名）")
    @GetMapping("/stats/hot-room-types")
    public Result<List<HotRoomTypeCountVO>> getHotRoomTypes(
            @Parameter(description = "排名数量，默认前6名") @RequestParam(defaultValue = "6") int topN,
            @Parameter(description = "统计最近天数，默认30天") @RequestParam(defaultValue = "30") int days) {
        return Result.ok(orderService.getHotRoomTypeCounts(topN, days));
    }

    /** 查询指定房间的预订时间线 */
    @Operation(summary = "查询指定房间的预订时间线（未来订单）")
    @GetMapping("/room/{roomId}/schedule")
    public Result<List<OrderVO>> getRoomSchedule(
            @Parameter(description = "房间ID") @PathVariable Long roomId,
            @Parameter(description = "起始日期(含)，默认今天") @RequestParam(required = false) String startDate,
            @Parameter(description = "结束日期(含)，默认30天后") @RequestParam(required = false) String endDate) {
        return Result.ok(orderService.getRoomSchedule(roomId, startDate, endDate));
    }
}
