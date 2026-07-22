package com.lhf.hotel.order.controller;

import com.lhf.hotel.common.result.PageResult;
import com.lhf.hotel.common.result.Result;
import com.lhf.hotel.order.model.dto.ServiceRequestDTO;
import com.lhf.hotel.order.model.entity.ServiceRequest;
import com.lhf.hotel.order.service.ServiceRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/order/service-request")
@RequiredArgsConstructor
@Tag(name = "服务呼叫", description = "客户呼叫人员（打扫/送物/维修等）与处理")
public class ServiceRequestController {

    private final ServiceRequestService serviceRequestService;

    @Operation(summary = "客户发起服务呼叫")
    @PostMapping
    public Result<ServiceRequest> create(@Valid @RequestBody ServiceRequestDTO dto) {
        return Result.ok(serviceRequestService.create(dto));
    }

    @Operation(summary = "客户查看我的服务呼叫")
    @GetMapping("/my")
    public Result<List<ServiceRequest>> listMine() {
        return Result.ok(serviceRequestService.listMine());
    }

    @Operation(summary = "管理侧：服务呼叫分页列表")
    @GetMapping("/admin")
    public Result<PageResult<ServiceRequest>> pageAdmin(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(serviceRequestService.pageAdmin(status, page, size));
    }

    @Operation(summary = "管理侧：处理 / 取消服务呼叫")
    @PutMapping("/{id}/handle")
    public Result<ServiceRequest> handle(
            @PathVariable Long id,
            @RequestParam String status,
            @RequestParam(required = false) String handleRemark) {
        return Result.ok(serviceRequestService.handle(id, status, handleRemark));
    }
}
