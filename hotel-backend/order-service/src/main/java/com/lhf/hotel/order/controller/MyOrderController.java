package com.lhf.hotel.order.controller;

import com.lhf.hotel.common.result.PageResult;
import com.lhf.hotel.common.result.Result;
import com.lhf.hotel.common.model.vo.OrderVO;
import com.lhf.hotel.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "客户端-我的订单", description = "会员查看自己的订单")
@RestController
@RequestMapping("/api/order/my")
@RequiredArgsConstructor
public class MyOrderController {

    private final OrderService orderService;

    /** 我的订单列表（客户端） */
    @Operation(summary = "我的订单列表（客户端）")
    @GetMapping
    public Result<PageResult<OrderVO>> myList(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") Integer size,
            @Parameter(description = "订单状态") @RequestParam(required = false) String status) {
        return Result.ok(orderService.myList(page, size, status));
    }

    /** 我的订单详情（客户端） */
    @Operation(summary = "我的订单详情（客户端）")
    @GetMapping("/{id}")
    public Result<OrderVO> myGetById(@Parameter(description = "订单ID") @PathVariable Long id) {
        return Result.ok(orderService.myGetById(id));
    }
}
