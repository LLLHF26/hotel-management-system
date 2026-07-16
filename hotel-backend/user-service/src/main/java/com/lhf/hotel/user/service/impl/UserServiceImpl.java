package com.lhf.hotel.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lhf.hotel.common.asserts.Assert;
import com.lhf.hotel.common.dto.CleaningTaskDTO;
import com.lhf.hotel.common.feign.RoomClient;
import com.lhf.hotel.common.result.PageResult;
import com.lhf.hotel.common.result.Result;
import com.lhf.hotel.user.mapper.UserMapper;
import com.lhf.hotel.user.model.dto.*;
import com.lhf.hotel.user.model.entity.User;
import com.lhf.hotel.user.model.vo.CleanerTaskVO;
import com.lhf.hotel.user.model.vo.CleanerVO;
import com.lhf.hotel.user.model.vo.UserVO;
import com.lhf.hotel.user.service.UserService;
import com.lhf.hotel.user.support.UserConverters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoomClient roomClient;

    public UserServiceImpl(UserMapper userMapper, PasswordEncoder passwordEncoder, RoomClient roomClient) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.roomClient = roomClient;
    }

    @Override
    public PageResult<UserVO> listUsers(int page, int size, String keyword, String role, Integer status) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(User::getUsername, keyword)
                    .or()
                    .like(User::getRealName, keyword));
        }
        if (StringUtils.hasText(role)) {
            wrapper.eq(User::getRole, role);
        }
        if (status != null) {
            wrapper.eq(User::getStatus, status);
        }
        wrapper.orderByDesc(User::getCreateTime);

        Page<User> result = userMapper.selectPage(new Page<>(page, size), wrapper);
        List<UserVO> records = result.getRecords().stream()
                .map(UserConverters::toUserVO)
                .toList();
        return PageResult.of(result.getTotal(), page, size, records);
    }

    @Override
    public UserVO getUserById(Long id) {
        User user = userMapper.selectById(id);
        Assert.notNull(user, "员工不存在");
        return UserConverters.toUserVO(user);
    }

    @Override
    public Long createUser(UserCreateDTO dto) {
        long exists = userMapper.selectCount(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername()));
        Assert.isFalse(exists > 0, "用户名已存在");

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRealName(dto.getRealName());
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        user.setRole(dto.getRole());
        user.setStatus(1);
        userMapper.insert(user);
        return user.getId();
    }

    @Override
    public void updateUser(Long id, UserUpdateDTO dto) {
        User user = userMapper.selectById(id);
        Assert.notNull(user, "员工不存在");
        if (dto.getRealName() != null) {
            user.setRealName(dto.getRealName());
        }
        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone());
        }
        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
        }
        if (dto.getRole() != null) {
            user.setRole(dto.getRole());
        }
        userMapper.updateById(user);
    }

    @Override
    public void deleteUser(Long id, Long currentUserId) {
        Assert.isFalse(id.equals(currentUserId), "不能删除自己");
        User user = userMapper.selectById(id);
        Assert.notNull(user, "员工不存在");
        userMapper.deleteById(id);
    }

    @Override
    public void updateStatus(Long id, StatusDTO dto) {
        User user = userMapper.selectById(id);
        Assert.notNull(user, "员工不存在");
        user.setStatus(dto.getStatus());
        userMapper.updateById(user);
    }

    @Override
    public void updateRole(Long id, RoleDTO dto) {
        User user = userMapper.selectById(id);
        Assert.notNull(user, "员工不存在");
        user.setRole(dto.getRole());
        userMapper.updateById(user);
    }

    @Override
    public List<CleanerVO> listCleaners() {
        List<User> users = userMapper.selectList(
                new LambdaQueryWrapper<User>()
                        .eq(User::getRole, "CLEANER")
                        .eq(User::getStatus, 1)
                        .orderByAsc(User::getId));
        return users.stream()
                .map(u -> CleanerVO.builder()
                        .id(u.getId())
                        .realName(u.getRealName())
                        .phone(u.getPhone())
                        .currentTaskCount(countActiveTasks(u.getId()))
                        .build())
                .toList();
    }

    @Override
    public PageResult<CleanerTaskVO> getCleanerTasks(Long cleanerId, int page, int size) {
        User cleaner = userMapper.selectById(cleanerId);
        Assert.notNull(cleaner, "保洁员不存在");
        Assert.isTrue("CLEANER".equals(cleaner.getRole()), "该用户不是保洁员");

        try {
            Result<PageResult<CleaningTaskDTO>> result = roomClient.listCleaningTasks(page, size, null, cleanerId, null);
            if (result != null && result.getCode() == 200 && result.getData() != null) {
                PageResult<CleaningTaskDTO> data = result.getData();
                List<CleanerTaskVO> records = data.getRecords() == null
                        ? Collections.emptyList()
                        : data.getRecords().stream().map(UserConverters::toCleanerTaskVO).toList();
                return PageResult.of(data.getTotal(), data.getPage(), data.getSize(), records);
            }
        } catch (Exception e) {
            log.warn("查询保洁员打扫记录失败 cleanerId={}: {}", cleanerId, e.getMessage());
        }
        return PageResult.empty(page, size);
    }

    @Override
    public UserVO getProfile(Long userId) {
        return getUserById(userId);
    }

    @Override
    public void updateProfile(Long userId, UserUpdateDTO dto) {
        User user = userMapper.selectById(userId);
        Assert.notNull(user, "员工不存在");
        if (dto.getRealName() != null) {
            user.setRealName(dto.getRealName());
        }
        if (dto.getPhone() != null) {
            user.setPhone(dto.getPhone());
        }
        if (dto.getEmail() != null) {
            user.setEmail(dto.getEmail());
        }
        if (dto.getAvatar() != null) {
            user.setAvatar(dto.getAvatar());
        }
        userMapper.updateById(user);
    }

    @Override
    public void changePassword(Long userId, PasswordDTO dto) {
        User user = userMapper.selectById(userId);
        Assert.notNull(user, "员工不存在");
        Assert.isTrue(passwordEncoder.matches(dto.getOldPassword(), user.getPassword()), "旧密码错误");
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userMapper.updateById(user);
    }

    private int countActiveTasks(Long cleanerId) {
        try {
            Result<PageResult<CleaningTaskDTO>> result = roomClient.listCleaningTasks(1, 1, "打扫中", cleanerId, null);
            if (result != null && result.getCode() == 200 && result.getData() != null) {
                return (int) Math.min(result.getData().getTotal(), Integer.MAX_VALUE);
            }
        } catch (Exception e) {
            log.warn("查询保洁员任务数失败 cleanerId={}: {}", cleanerId, e.getMessage());
        }
        return 0;
    }
}
