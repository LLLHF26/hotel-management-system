package com.lhf.hotel.order.controller;

import com.lhf.hotel.common.result.PageResult;
import com.lhf.hotel.common.result.Result;
import com.lhf.hotel.order.model.dto.ProductDTO;
import com.lhf.hotel.order.model.entity.Product;
import com.lhf.hotel.order.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/order/products")
@RequiredArgsConstructor
@Tag(name = "酒店商品", description = "客房商品（食物/饮用水/日用品等）的展示与管理")
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "客户侧：可用商品列表（含积分抵扣配置）")
    @GetMapping
    public Result<Map<String, Object>> listAvailable() {
        return Result.ok(productService.listAvailableWithConfig());
    }

    @Operation(summary = "管理侧：商品分页列表")
    @GetMapping("/admin")
    public Result<PageResult<Product>> pageAdmin(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(productService.pageAdmin(keyword, page, size));
    }

    @Operation(summary = "管理侧：商品详情")
    @GetMapping("/{id}")
    public Result<Product> getById(@PathVariable Long id) {
        return Result.ok(productService.getById(id));
    }

    @Operation(summary = "管理侧：新建商品")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody ProductDTO dto) {
        productService.create(dto);
        return Result.ok();
    }

    @Operation(summary = "管理侧：编辑商品")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ProductDTO dto) {
        productService.update(id, dto);
        return Result.ok();
    }

    @Operation(summary = "管理侧：删除商品")
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        productService.remove(id);
        return Result.ok();
    }
}
