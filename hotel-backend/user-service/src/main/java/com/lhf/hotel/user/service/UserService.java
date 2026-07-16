package com.lhf.hotel.user.service;

import com.lhf.hotel.common.result.PageResult;
import com.lhf.hotel.user.model.dto.*;
import com.lhf.hotel.user.model.vo.CleanerTaskVO;
import com.lhf.hotel.user.model.vo.CleanerVO;
import com.lhf.hotel.user.model.vo.UserVO;

import java.util.List;

public interface UserService {

    // ========== 员工管理 ==========

    PageResult<UserVO> listUsers(int page, int size, String keyword, String role, Integer status);

    UserVO getUserById(Long id);

    Long createUser(UserCreateDTO dto);

    void updateUser(Long id, UserUpdateDTO dto);

    void deleteUser(Long id, Long currentUserId);

    void updateStatus(Long id, StatusDTO dto);

    void updateRole(Long id, RoleDTO dto);

    // ========== 保洁员 ==========

    List<CleanerVO> listCleaners();

    PageResult<CleanerTaskVO> getCleanerTasks(Long cleanerId, int page, int size);

    // ========== 个人中心 ==========

    UserVO getProfile(Long userId);

    void updateProfile(Long userId, UserUpdateDTO dto);

    void changePassword(Long userId, PasswordDTO dto);
}
