package com.lhf.hotel.user.controller;

import com.lhf.hotel.common.result.PageResult;
import com.lhf.hotel.common.result.Result;
import com.lhf.hotel.user.model.dto.*;
import com.lhf.hotel.user.model.vo.CleanerTaskVO;
import com.lhf.hotel.user.model.vo.CleanerVO;
import com.lhf.hotel.user.model.vo.UserVO;
import com.lhf.hotel.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "员工管理", description = "员工CRUD、角色管理、个人中心、保洁员查询")
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ==================== 员工管理 ====================

    /** 员工列表 */
    @Operation(summary = "员工列表")
    @GetMapping("/list")
    public Result<PageResult<UserVO>> list(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "搜索关键字") @RequestParam(required = false) String keyword,
            @Parameter(description = "角色") @RequestParam(required = false) String role,
            @Parameter(description = "状态：1-启用，0-禁用") @RequestParam(required = false) Integer status) {
        PageResult<UserVO> result = userService.listUsers(page, size, keyword, role, status);
        return Result.ok(result);
    }

    /** 员工详情 */
    @Operation(summary = "员工详情")
    @GetMapping("/{id}")
    public Result<UserVO> detail(@Parameter(description = "员工ID") @PathVariable Long id) {
        return Result.ok(userService.getUserById(id));
    }

    /** 创建员工 */
    @Operation(summary = "创建员工")
    @PostMapping("/create")
    public Result<?> create(@Parameter(description = "创建员工请求参数") @Valid @RequestBody UserCreateDTO dto) {
        Long id = userService.createUser(dto);
        return Result.ok("创建成功", java.util.Map.of("id", id));
    }

    /** 修改员工 */
    @Operation(summary = "修改员工")
    @PutMapping("/{id}")
    public Result<Void> update(@Parameter(description = "员工ID") @PathVariable Long id,
                                @Parameter(description = "修改员工请求参数") @Valid @RequestBody UserUpdateDTO dto) {
        userService.updateUser(id, dto);
        return Result.ok("修改成功", null);
    }

    /** 删除员工 */
    @Operation(summary = "删除员工")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@Parameter(description = "员工ID") @PathVariable Long id,
                               @Parameter(description = "当前登录用户ID", required = true) @RequestHeader("X-UserId") Long currentUserId) {
        userService.deleteUser(id, currentUserId);
        return Result.ok("删除成功", null);
    }

    /** 启用/禁用员工 */
    @Operation(summary = "启用/禁用员工")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@Parameter(description = "员工ID") @PathVariable Long id,
                                      @Parameter(description = "状态请求参数") @Valid @RequestBody StatusDTO dto) {
        userService.updateStatus(id, dto);
        return Result.ok(dto.getStatus() == 1 ? "已启用" : "已禁用", null);
    }

    /** 修改员工角色 */
    @Operation(summary = "修改员工角色")
    @PutMapping("/{id}/role")
    public Result<Void> updateRole(@Parameter(description = "员工ID") @PathVariable Long id,
                                    @Parameter(description = "角色请求参数") @Valid @RequestBody RoleDTO dto) {
        userService.updateRole(id, dto);
        return Result.ok("角色修改成功", null);
    }

    // ==================== 保洁员 ====================

    /** 保洁员列表 */
    @Operation(summary = "保洁员列表")
    @GetMapping("/cleaner/list")
    public Result<List<CleanerVO>> cleanerList() {
        return Result.ok(userService.listCleaners());
    }

    /** 保洁员打扫记录 */
    @Operation(summary = "保洁员打扫记录")
    @GetMapping("/cleaner/{id}/tasks")
    public Result<PageResult<CleanerTaskVO>> cleanerTasks(
            @Parameter(description = "保洁员ID") @PathVariable Long id,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") int size) {
        return Result.ok(userService.getCleanerTasks(id, page, size));
    }

    // ==================== 个人中心 ====================

    /** 查看自己信息 */
    @Operation(summary = "查看自己信息")
    @GetMapping("/profile")
    public Result<UserVO> profile(@Parameter(description = "当前登录用户ID", required = true) @RequestHeader("X-UserId") Long userId) {
        return Result.ok(userService.getProfile(userId));
    }

    /** 修改自己信息 */
    @Operation(summary = "修改自己信息")
    @PutMapping("/profile")
    public Result<Void> updateProfile(@Parameter(description = "当前登录用户ID", required = true) @RequestHeader("X-UserId") Long userId,
                                       @Parameter(description = "修改信息请求参数") @Valid @RequestBody UserUpdateDTO dto) {
        userService.updateProfile(userId, dto);
        return Result.ok("修改成功", null);
    }

    /** 修改密码 */
    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public Result<Void> changePassword(@Parameter(description = "当前登录用户ID", required = true) @RequestHeader("X-UserId") Long userId,
                                        @Parameter(description = "修改密码请求参数") @Valid @RequestBody PasswordDTO dto) {
        userService.changePassword(userId, dto);
        return Result.ok("密码修改成功，请重新登录", null);
    }
}
