package com.lhf.hotel.order.controller;

import com.lhf.hotel.common.result.PageResult;
import com.lhf.hotel.common.result.Result;
import com.lhf.hotel.common.model.vo.OrderVO;
import com.lhf.hotel.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/order/my")
@RequiredArgsConstructor
public class MyOrderController {

    private final OrderService orderService;

    @GetMapping
    public Result<PageResult<OrderVO>> myList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String status) {
        return Result.ok(orderService.myList(page, size, status));
    }

    @GetMapping("/{id}")
    public Result<OrderVO> myGetById(@PathVariable Long id) {
        return Result.ok(orderService.myGetById(id));
    }
}
