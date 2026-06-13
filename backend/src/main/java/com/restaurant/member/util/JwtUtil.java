package com.restaurant.member.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

/**
 * JWT工具類別：負責 JSON Web Token 的簽發、驗證與解析。
 */
@Component
public class JwtUtil {
    //
    private static final Logger log = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration:86400000}")
    private long expirationMs;

    private SecretKey getSigningKey() {
        try {
            // 使用 SHA-256 將任意長度的 secret 轉換為固定的 256 bits (32 bytes)
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(secret.getBytes(StandardCharsets.UTF_8));
            return Keys.hmacShaKeyFor(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("初始化 JWT 簽章失敗", e);
        }
    }

    /**
     * 【核心方法】產生 JWT Token 通行證
     * 當使用者登入驗證成功後，呼叫此方法製造 Token 回傳給前端。
     */
    public String generateToken(Long userId, String roleName) {
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("role", roleName)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long getUserId(String token) {
        return Long.parseLong(parseToken(token).getSubject());
    }

    public String getRole(String token) {
        return parseToken(token).get("role", String.class);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build().parseSignedClaims(token);
            return true;
        } catch (io.jsonwebtoken.security.SignatureException e) {
            log.warn("無效的 JWT 簽名: {}", e.getMessage());
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.warn("JWT Token 已過期: {}", e.getMessage());
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            log.warn("不合法的 JWT Token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT 參數錯誤: {}", e.getMessage());
        }
        return false;
    }
}