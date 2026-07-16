package com.restaurant.config;

import com.restaurant.member.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT 驗證過濾器。
 *
 * 每一個 HTTP Request 進來時執行一次，負責：
 * 1. 優先從 Authorization Header 取 Bearer Token，否則讀 HttpOnly Cookie
 * 2. 用 JwtUtil 驗證 Token 是否合法
 * 3. 解析出 userId 和 roleName，寫入 SecurityContextHolder
 *
 * Controller 透過 @AuthenticationPrincipal 或 *
 * SecurityContextHolder.getContext().getAuthentication() 取得當前使用者資訊，
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        String token = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        } else if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("access_token".equals(cookie.getName())) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

        // 沒有 Token，直接放行（交給 SecurityConfig 決定是否拒絕）
        if (token == null || token.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        // Token 驗證失敗（過期、被篡改等），直接放行，SecurityConfig 會擋下需要認證的路由
        if (!jwtUtil.validateToken(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 從 Token 解析 userId 和 roleName
        Long userId = jwtUtil.getUserId(token);
        String roleName = jwtUtil.getRole(token);

        // 將角色包裝成 Spring Security 的權限格式（需加上 ROLE_ 前綴）
        String authority = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;

        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(authority));

        // 建立認證物件，principal 存 userId，方便 Controller 直接取用
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userId, null,
                authorities);

        // 寫入 SecurityContext，後續的 Filter 和 Controller 都能讀到
        SecurityContextHolder.getContext().setAuthentication(authentication);

        filterChain.doFilter(request, response);
    }
}
