package com.lhf.hotel.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

/**
 * JWT 工具 —— 生成、解析、校验 Token
 */
public final class JwtUtil {

    private JwtUtil() {
    }

    /** 密钥（生产环境应从配置中心读取） */
    private static final String SECRET = "hotel-system-jwt-secret-key-2026-must-be-at-least-256-bits-long!";
    private static final SecretKey KEY = Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    /** Token 有效期：24 小时 */
    private static final long EXPIRE_MS = 24 * 60 * 60 * 1000L;

    /** 生成 Token */
    public static String generate(Long userId, String username, String role, Map<String, Object> extra) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("role", role)
                .claims(extra)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + EXPIRE_MS))
                .signWith(KEY)
                .compact();
    }

    /** 解析 Token 中的所有 Claims */
    public static Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** 校验 Token 是否有效 */
    public static boolean validate(String token) {
        try {
            parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /** 从 Token 中提取 userId */
    public static Long getUserId(String token) {
        return Long.valueOf(parse(token).getSubject());
    }

    /** 从 Token 中提取 role */
    public static String getUserRole(String token) {
        return parse(token).get("role", String.class);
    }

    /** 从 Token 中提取 username */
    public static String getUsername(String token) {
        return parse(token).get("username", String.class);
    }

    /** Token 过期时间 */
    public static Date getExpiration(String token) {
        return parse(token).getExpiration();
    }

    /** Token 剩余有效毫秒数 */
    public static long getRemainingMs(String token) {
        Date expiration = getExpiration(token);
        long remaining = expiration.getTime() - System.currentTimeMillis();
        return Math.max(remaining, 0);
    }

    public static final String BLACKLIST_PREFIX = "jwt:blacklist:";
}