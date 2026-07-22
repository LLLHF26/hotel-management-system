package com.lhf.hotel.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lhf.hotel.common.asserts.Assert;
import com.lhf.hotel.common.constant.ErrorCode;
import com.lhf.hotel.common.exception.BusinessException;
import com.lhf.hotel.common.util.JwtUtil;
import com.lhf.hotel.common.util.RedisUtil;
import com.lhf.hotel.user.mapper.CustomerMapper;
import com.lhf.hotel.user.mapper.UserMapper;
import com.lhf.hotel.user.model.dto.LoginDTO;
import com.lhf.hotel.user.model.entity.Customer;
import com.lhf.hotel.user.model.entity.User;
import com.lhf.hotel.user.model.vo.LoginVO;
import com.lhf.hotel.user.model.vo.RefreshVO;
import com.lhf.hotel.user.service.AuthService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final CustomerMapper customerMapper;
    private final PasswordEncoder passwordEncoder;
    private final RedisUtil redisUtil;

    public AuthServiceImpl(UserMapper userMapper, CustomerMapper customerMapper,
                           PasswordEncoder passwordEncoder, RedisUtil redisUtil) {
        this.userMapper = userMapper;
        this.customerMapper = customerMapper;
        this.passwordEncoder = passwordEncoder;
        this.redisUtil = redisUtil;
    }

    @Override
    public LoginVO login(LoginDTO dto) {
        // 1) 先查员工表
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername()));
        if (user != null) {
            Assert.isTrue(user.getStatus() == 1, "账号已被禁用");
            Assert.isTrue(passwordEncoder.matches(dto.getPassword(), user.getPassword()), "用户名或密码错误");
            String token = JwtUtil.generate(user.getId(), user.getUsername(), user.getRole(), Map.of());
            return LoginVO.builder()
                    .token(token)
                    .userId(user.getId())
                    .username(user.getUsername())
                    .realName(user.getRealName())
                    .role(user.getRole())
                    .build();
        }

        // 2) 再查会员表（手机号作为登录凭证）
        Customer customer = customerMapper.selectOne(new LambdaQueryWrapper<Customer>()
                .eq(Customer::getPhone, dto.getUsername()));
        if (customer == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST.getCode(), "用户名或密码错误");
        }
        Assert.isTrue(customer.getStatus() == 1, "账号已被冻结");
        Assert.hasText(customer.getPassword(), "该账号尚未设置密码，请先注册");
        Assert.isTrue(passwordEncoder.matches(dto.getPassword(), customer.getPassword()), "用户名或密码错误");

        String token = JwtUtil.generate(customer.getId(), customer.getPhone(),
                "CUSTOMER", Map.of());

        return LoginVO.builder()
                .token(token)
                .userId(customer.getId())
                .username(customer.getPhone())
                .realName(customer.getRealName())
                .role("CUSTOMER")
                .build();
    }

    @Override
    public RefreshVO refreshToken(String oldToken) {
        Assert.isTrue(JwtUtil.validate(oldToken), "Token 无效或已过期");
        Assert.isFalse(isTokenBlacklisted(oldToken), "Token 已失效");

        Long userId = JwtUtil.getUserId(oldToken);
        String username = JwtUtil.getUsername(oldToken);
        String role = JwtUtil.getUserRole(oldToken);

        String newToken = JwtUtil.generate(userId, username, role, Map.of());
        Date expiration = JwtUtil.getExpiration(newToken);
        LocalDateTime expireAt = LocalDateTime.ofInstant(expiration.toInstant(), ZoneId.systemDefault());

        return RefreshVO.builder()
                .token(newToken)
                .expireAt(expireAt)
                .build();
    }

    @Override
    public void logout(String token) {
        if (!JwtUtil.validate(token)) {
            return;
        }
        long remainingMs = JwtUtil.getRemainingMs(token);
        if (remainingMs > 0) {
            redisUtil.set(JwtUtil.BLACKLIST_PREFIX + token, "1", remainingMs, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public boolean isTokenBlacklisted(String token) {
        return Boolean.TRUE.equals(redisUtil.hasKey(JwtUtil.BLACKLIST_PREFIX + token));
    }
}
