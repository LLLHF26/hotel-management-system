package com.lhf.hotel.user.service;

import com.lhf.hotel.user.model.dto.LoginDTO;
import com.lhf.hotel.user.model.vo.LoginVO;
import com.lhf.hotel.user.model.vo.RefreshVO;

public interface AuthService {

    /** 登录 */
    LoginVO login(LoginDTO dto);

    /** 刷新 Token */
    RefreshVO refreshToken(String oldToken);

    /** 登出（Token 加入黑名单） */
    void logout(String token);

    /** Token 是否在黑名单 */
    boolean isTokenBlacklisted(String token);
}
