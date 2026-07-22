package com.lhf.hotel.order.controller;

import com.lhf.hotel.common.result.PageResult;
import com.lhf.hotel.common.result.Result;
import com.lhf.hotel.order.model.dto.ExtraDTO;
import com.lhf.hotel.common.model.vo.ExtraVO;
import com.lhf.hotel.order.model.vo.ProductOrderVO;
import com.lhf.hotel.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "额外消费管理", description = "订单的餐饮、洗衣等额外消费")
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class ExtraController {

    private final OrderService orderService;

    /** 添加额外消费（如餐饮、洗衣等） */
    @Operation(summary = "添加额外消费（如餐饮、洗衣等）")
    @PostMapping("/{id}/extra")
    public Result<Map<String, Object>> addExtra(@Parameter(description = "订单ID") @PathVariable Long id, @Parameter(description = "额外消费信息") @Valid @RequestBody ExtraDTO dto) {
        return Result.ok("消费已添加", orderService.addExtra(id, dto));
    }

    /** 订单额外消费列表 */
    @Operation(summary = "订单额外消费列表")
    @GetMapping("/{id}/extras")
    public Result<List<ExtraVO>> getExtras(@Parameter(description = "订单ID") @PathVariable Long id) {
        return Result.ok(orderService.getExtras(id));
    }

    /** 删除额外消费记录 */
    @Operation(summary = "删除额外消费记录")
    @DeleteMapping("/{id}/extra/{extraId}")
    public Result<Void> deleteExtra(@Parameter(description = "订单ID") @PathVariable Long id, @Parameter(description = "额外消费记录ID") @PathVariable Long extraId) {
        orderService.deleteExtra(id, extraId);
        return Result.ok("已删除", null);
    }

    /** 最近客房商品订单（前台配送提醒，关联订单号/房间/客户） */
    @Operation(summary = "最近客房商品订单（前台配送提醒）")
    @GetMapping("/product-orders/recent")
    public Result<PageResult<ProductOrderVO>> recentProductOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.ok(orderService.recentProductOrders(page, size));
    }
}
