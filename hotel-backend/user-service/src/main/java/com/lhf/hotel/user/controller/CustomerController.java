package com.lhf.hotel.user.controller;

import com.lhf.hotel.common.result.PageResult;
import com.lhf.hotel.common.result.Result;
import com.lhf.hotel.user.model.dto.*;
import com.lhf.hotel.user.model.vo.CustomerVO;
import com.lhf.hotel.user.model.vo.PointsLogVO;
import com.lhf.hotel.user.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customer")
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    // ==================== 会员管理（管理端） ====================

    /** 会员列表 */
    @GetMapping("/list")
    public Result<PageResult<CustomerVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String memberLevel,
            @RequestParam(required = false) Integer status) {
        return Result.ok(customerService.listCustomers(page, size, keyword, memberLevel, status));
    }

    /** 会员详情 */
    @GetMapping("/{id}")
    public Result<CustomerVO> detail(@PathVariable Long id) {
        return Result.ok(customerService.getCustomerById(id));
    }

    /** 会员注册（客户端免登录） */
    @PostMapping("/register")
    public Result<?> register(@Valid @RequestBody CustomerRegisterDTO dto) {
        Long id = customerService.register(dto);
        return Result.ok("注册成功", java.util.Map.of("id", id));
    }

    /** 修改会员信息 */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody CustomerUpdateDTO dto) {
        customerService.updateCustomer(id, dto);
        return Result.ok("修改成功", null);
    }

    /** 冻结/解冻会员 */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusDTO dto) {
        customerService.updateStatus(id, dto);
        return Result.ok(dto.getStatus() == 1 ? "已解冻" : "已冻结", null);
    }

    /** 积分记录 */
    @GetMapping("/{id}/points")
    public Result<PageResult<PointsLogVO>> pointsLog(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.ok(customerService.getPointsLog(id, page, size));
    }

    /** 增加积分（内部调用） */
    @PostMapping("/{id}/points/add")
    public Result<Void> addPoints(
            @PathVariable Long id,
            @RequestParam Integer points,
            @RequestParam String reason) {
        customerService.addPoints(id, points, reason);
        return Result.ok(null);
    }

    /** 增加累计消费（内部调用） */
    @PostMapping("/{id}/consumed/add")
    public Result<Void> addConsumed(
            @PathVariable Long id,
            @RequestParam java.math.BigDecimal amount) {
        customerService.addConsumed(id, amount);
        return Result.ok(null);
    }

    // ==================== 会员个人中心（客户端） ====================

    /** 查看自己信息 */
    @GetMapping("/profile")
    public Result<CustomerVO> profile(@RequestHeader("X-UserId") Long customerId) {
        return Result.ok(customerService.getProfile(customerId));
    }

    /** 修改自己信息 */
    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestHeader("X-UserId") Long customerId,
                                       @Valid @RequestBody CustomerUpdateDTO dto) {
        customerService.updateProfile(customerId, dto);
        return Result.ok("修改成功", null);
    }

    /** 修改密码 */
    @PutMapping("/password")
    public Result<Void> changePassword(@RequestHeader("X-UserId") Long customerId,
                                        @Valid @RequestBody PasswordDTO dto) {
        customerService.changePassword(customerId, dto);
        return Result.ok("密码修改成功，请重新登录", null);
    }
}
