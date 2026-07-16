package com.lhf.hotel.order.controller;

import com.lhf.hotel.common.result.Result;
import com.lhf.hotel.order.model.dto.ExtraDTO;
import com.lhf.hotel.common.model.vo.ExtraVO;
import com.lhf.hotel.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class ExtraController {

    private final OrderService orderService;

    @PostMapping("/{id}/extra")
    public Result<Map<String, Object>> addExtra(@PathVariable Long id, @Valid @RequestBody ExtraDTO dto) {
        return Result.ok("消费已添加", orderService.addExtra(id, dto));
    }

    @GetMapping("/{id}/extras")
    public Result<List<ExtraVO>> getExtras(@PathVariable Long id) {
        return Result.ok(orderService.getExtras(id));
    }

    @DeleteMapping("/{id}/extra/{extraId}")
    public Result<Void> deleteExtra(@PathVariable Long id, @PathVariable Long extraId) {
        orderService.deleteExtra(id, extraId);
        return Result.ok("已删除", null);
    }
}
