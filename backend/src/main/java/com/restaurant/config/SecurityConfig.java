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
                        .requestMatchers(HttpMethod.POST, "/api/members/password/forgot").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/members/password/reset").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/members/email/send-code").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/members/email/verify-code").permitAll()

                        // ===== 前台門市查詢（公開）=====
                        .requestMatchers("/api/stores/**").permitAll()

                        // ===== 前台菜單、菜單分類、分店菜單查詢 (公開) =====
                        .requestMatchers("/api/menu-component/**").permitAll()  
                        .requestMatchers("/api/menu-categories").permitAll()    
                        .requestMatchers("/api/menu-items/store/**").permitAll() 
                        

                        // ===== 訂單（Demo 測試先公開）=====
                        .requestMatchers(HttpMethod.POST, "/api/orders").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/orders/**").permitAll()
                        .requestMatchers("/api/payments/**").permitAll()

                        // ===== 會員路由（需要登入，CUSTOMER 角色）=====
                        .requestMatchers(HttpMethod.GET, "/api/members/me").hasAuthority("ROLE_CUSTOMER")
                        .requestMatchers(HttpMethod.GET, "/api/members/me/points/**").hasAuthority("ROLE_CUSTOMER")
                        .requestMatchers(HttpMethod.PUT, "/api/members/me").hasAuthority("ROLE_CUSTOMER")
                        .requestMatchers(HttpMethod.PUT, "/api/members/me/password").hasAuthority("ROLE_CUSTOMER")
                        .requestMatchers(HttpMethod.DELETE, "/api/members/me").hasAuthority("ROLE_CUSTOMER")

                        // ===== 後台菜單管理（需要後台權限，並支援多層級分店隔離路由）=====
                        // 🎯【權限設定全面擴充】
                        // 為什麼改用 /** 為了讓「信義店、南港店等分店隔離路由」能正常通車。
                        // 原本單星號 /* 只能管到一層（例如：/api/menu-items/1）
                        // 改成雙星號 /** 才能管到多層路徑（例如：/api/menu-items/1/store/1），避免分店店長修改菜單時被系統擋下(噴403)。
                        .requestMatchers("/api/menu-items/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER", "ROLE_STAFF")

                        // ===== 員工管理路由（需要 ADMIN 角色）=====
                        .requestMatchers(HttpMethod.POST, "/api/members/staff").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/api/members/staff/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER")

                        // ===== 後台門市管理（需要 ADMIN / MANAGER）=====
                        .requestMatchers("/api/admin/stores/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER")
                        .requestMatchers("/api/admin/tables/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER")

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
