package com.lhf.hotel.user.controller;

import com.lhf.hotel.common.result.PageResult;
import com.lhf.hotel.common.result.Result;
import com.lhf.hotel.user.model.dto.*;
import com.lhf.hotel.user.model.vo.CleanerTaskVO;
import com.lhf.hotel.user.model.vo.CleanerVO;
import com.lhf.hotel.user.model.vo.UserVO;
import com.lhf.hotel.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ==================== 员工管理 ====================

    /** 员工列表 */
    @GetMapping("/list")
    public Result<PageResult<UserVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Integer status) {
        PageResult<UserVO> result = userService.listUsers(page, size, keyword, role, status);
        return Result.ok(result);
    }

    /** 员工详情 */
    @GetMapping("/{id}")
    public Result<UserVO> detail(@PathVariable Long id) {
        return Result.ok(userService.getUserById(id));
    }

    /** 创建员工 */
    @PostMapping("/create")
    public Result<?> create(@Valid @RequestBody UserCreateDTO dto) {
        Long id = userService.createUser(dto);
        return Result.ok("创建成功", java.util.Map.of("id", id));
    }

    /** 修改员工 */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody UserUpdateDTO dto) {
        userService.updateUser(id, dto);
        return Result.ok("修改成功", null);
    }

    /** 删除员工 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id,
                               @RequestHeader("X-UserId") Long currentUserId) {
        userService.deleteUser(id, currentUserId);
        return Result.ok("删除成功", null);
    }

    /** 启用/禁用员工 */
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody StatusDTO dto) {
        userService.updateStatus(id, dto);
        return Result.ok(dto.getStatus() == 1 ? "已启用" : "已禁用", null);
    }

    /** 修改员工角色 */
    @PutMapping("/{id}/role")
    public Result<Void> updateRole(@PathVariable Long id, @Valid @RequestBody RoleDTO dto) {
        userService.updateRole(id, dto);
        return Result.ok("角色修改成功", null);
    }

    // ==================== 保洁员 ====================

    /** 保洁员列表 */
    @GetMapping("/cleaner/list")
    public Result<List<CleanerVO>> cleanerList() {
        return Result.ok(userService.listCleaners());
    }

    /** 保洁员打扫记录 */
    @GetMapping("/cleaner/{id}/tasks")
    public Result<PageResult<CleanerTaskVO>> cleanerTasks(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(userService.getCleanerTasks(id, page, size));
    }

    // ==================== 个人中心 ====================

    /** 查看自己信息 */
    @GetMapping("/profile")
    public Result<UserVO> profile(@RequestHeader("X-UserId") Long userId) {
        return Result.ok(userService.getProfile(userId));
    }

    /** 修改自己信息 */
    @PutMapping("/profile")
    public Result<Void> updateProfile(@RequestHeader("X-UserId") Long userId,
                                       @Valid @RequestBody UserUpdateDTO dto) {
        userService.updateProfile(userId, dto);
        return Result.ok("修改成功", null);
    }

    /** 修改密码 */
    @PutMapping("/password")
    public Result<Void> changePassword(@RequestHeader("X-UserId") Long userId,
                                        @Valid @RequestBody PasswordDTO dto) {
        userService.changePassword(userId, dto);
        return Result.ok("密码修改成功，请重新登录", null);
    }
}
