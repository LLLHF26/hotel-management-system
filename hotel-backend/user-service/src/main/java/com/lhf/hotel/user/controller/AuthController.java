package com.lhf.hotel.user.controller;

import com.lhf.hotel.common.result.Result;
import com.lhf.hotel.user.model.dto.LoginDTO;
import com.lhf.hotel.user.model.vo.LoginVO;
import com.lhf.hotel.user.model.vo.RefreshVO;
import com.lhf.hotel.user.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** 登录 */
    @PostMapping("/login")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO dto) {
        LoginVO vo = authService.login(dto);
        return Result.ok("登录成功", vo);
    }

    /** 刷新 Token */
    @PostMapping("/refresh")
    public Result<RefreshVO> refresh(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        return Result.ok("刷新成功", authService.refreshToken(token));
    }

    /** 登出 */
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        authService.logout(token);
        return Result.ok("登出成功", null);
    }
}
