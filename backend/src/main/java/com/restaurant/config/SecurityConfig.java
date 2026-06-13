package com.restaurant.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 主設定。
 * - Stateless：每個請求都靠 JWT 驗證
 * - CSRF 關閉：純 REST API 不需要 CSRF 保護
 * - CORS：已由 CorsConfig.java 統一管理，這裡不重複設定
 * - 路由權限：依角色（CUSTOMER / STAFF / MANAGER / ADMIN）分別設定
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // 關閉 CSRF（REST API 不需要）
                .csrf(AbstractHttpConfigurer::disable)

                // CORS 由 CorsConfig.java 的 CorsFilter Bean 負責
                .cors(cors -> {
                })

                // 使用 JWT
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 路由權限設定
                .authorizeHttpRequests(auth -> auth

                        // ===== 公開路由（不需要登入）=====
                        .requestMatchers(HttpMethod.POST, "/api/members/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/members/login").permitAll()

                        // ===== 會員路由（需要登入，CUSTOMER 角色）=====
                        .requestMatchers(HttpMethod.GET, "/api/members/me").hasAuthority("ROLE_CUSTOMER")
                        .requestMatchers(HttpMethod.PUT, "/api/members/me").hasAuthority("ROLE_CUSTOMER")
                        .requestMatchers(HttpMethod.PUT, "/api/members/me/password").hasAuthority("ROLE_CUSTOMER")
                        .requestMatchers(HttpMethod.DELETE, "/api/members/me").hasAuthority("ROLE_CUSTOMER")

                        // ===== 員工管理路由（需要 ADMIN 角色）=====
                        .requestMatchers(HttpMethod.POST, "/api/members/staff").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/api/members/staff/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER")

                        // 其餘所有請求都需要登入
                        .anyRequest().authenticated())

                // 將 JwtAuthenticationFilter 插在 Spring Security 內建的帳密驗證 Filter 之前
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 密碼加密器，全系統統一使用 BCrypt。
     * 宣告在這裡讓 Spring 管理，為 AuthServiceImpl 注入的 PasswordEncoder 。
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
