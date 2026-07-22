package com.lhf.hotel.user.controller;

import com.lhf.hotel.common.result.PageResult;
import com.lhf.hotel.common.result.Result;
import com.lhf.hotel.user.model.dto.*;
import com.lhf.hotel.user.model.vo.CustomerVO;
import com.lhf.hotel.user.model.vo.PointsLogVO;
import com.lhf.hotel.user.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "会员管理", description = "会员CRUD、积分管理、会员个人中心")
@RestController
@RequestMapping("/api/customer")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    // ==================== 会员管理（管理端） ====================

    /** 会员列表 */
    @Operation(summary = "会员列表")
    @GetMapping("/list")
    public Result<PageResult<CustomerVO>> list(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "搜索关键字") @RequestParam(required = false) String keyword,
            @Parameter(description = "会员等级") @RequestParam(required = false) String memberLevel,
            @Parameter(description = "状态：1-正常，0-冻结") @RequestParam(required = false) Integer status) {
        return Result.ok(customerService.listCustomers(page, size, keyword, memberLevel, status));
    }

    /** 会员详情 */
    @Operation(summary = "会员详情")
    @GetMapping("/{id}")
    public Result<CustomerVO> detail(@Parameter(description = "会员ID") @PathVariable Long id) {
        return Result.ok(customerService.getCustomerById(id));
    }

    /** 会员注册（客户端免登录） */
    @Operation(summary = "会员注册")
    @PostMapping("/register")
    public Result<?> register(@Parameter(description = "会员注册请求参数") @Valid @RequestBody CustomerRegisterDTO dto) {
        Long id = customerService.register(dto);
        return Result.ok("注册成功", java.util.Map.of("id", id));
    }

    /** 修改会员信息 */
    @Operation(summary = "修改会员信息")
    @PutMapping("/{id}")
    public Result<Void> update(@Parameter(description = "会员ID") @PathVariable Long id,
                                @Parameter(description = "修改会员请求参数") @Valid @RequestBody CustomerUpdateDTO dto) {
        customerService.updateCustomer(id, dto);
        return Result.ok("修改成功", null);
    }

    /** 冻结/解冻会员 */
    @Operation(summary = "冻结/解冻会员")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@Parameter(description = "会员ID") @PathVariable Long id,
                                      @Parameter(description = "状态请求参数") @Valid @RequestBody StatusDTO dto) {
        customerService.updateStatus(id, dto);
        return Result.ok(dto.getStatus() == 1 ? "已解冻" : "已冻结", null);
    }

    /** 积分记录 */
    @Operation(summary = "积分记录")
    @GetMapping("/{id}/points")
    public Result<PageResult<PointsLogVO>> pointsLog(
            @Parameter(description = "会员ID") @PathVariable Long id,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") int size) {
        return Result.ok(customerService.getPointsLog(id, page, size));
    }

    /** 增加积分（内部调用） */
    @Operation(summary = "增加积分")
    @PostMapping("/{id}/points/add")
    public Result<Void> addPoints(
            @Parameter(description = "会员ID") @PathVariable Long id,
            @Parameter(description = "积分数") @RequestParam Integer points,
            @Parameter(description = "增加原因") @RequestParam String reason) {
        customerService.addPoints(id, points, reason);
        return Result.ok(null);
    }

    /** 增加累计消费（内部调用） */
    @Operation(summary = "增加累计消费")
    @PostMapping("/{id}/consumed/add")
    public Result<Void> addConsumed(
            @Parameter(description = "会员ID") @PathVariable Long id,
            @Parameter(description = "消费金额") @RequestParam java.math.BigDecimal amount) {
        customerService.addConsumed(id, amount);
        return Result.ok(null);
    }

    // ==================== 会员个人中心（客户端） ====================

    /** 查看自己信息 */
    @Operation(summary = "查看自己信息")
    @GetMapping("/profile")
    public Result<CustomerVO> profile(@Parameter(description = "当前登录会员ID", required = true) @RequestHeader("X-UserId") Long customerId) {
        return Result.ok(customerService.getProfile(customerId));
    }

    /** 修改自己信息 */
    @Operation(summary = "修改自己信息")
    @PutMapping("/profile")
    public Result<Void> updateProfile(@Parameter(description = "当前登录会员ID", required = true) @RequestHeader("X-UserId") Long customerId,
                                       @Parameter(description = "修改信息请求参数") @Valid @RequestBody CustomerUpdateDTO dto) {
        customerService.updateProfile(customerId, dto);
        return Result.ok("修改成功", null);
    }

    /** 修改密码 */
    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public Result<Void> changePassword(@Parameter(description = "当前登录会员ID", required = true) @RequestHeader("X-UserId") Long customerId,
                                        @Parameter(description = "修改密码请求参数") @Valid @RequestBody PasswordDTO dto) {
        customerService.changePassword(customerId, dto);
        return Result.ok("密码修改成功，请重新登录", null);
    }
}
