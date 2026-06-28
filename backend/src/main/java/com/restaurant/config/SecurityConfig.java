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
 * - CORS：由 CorsConfig.java 統一管理
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

                // 使用 JWT，不使用 Session
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 路由權限設定
                .authorizeHttpRequests(auth -> auth

                        // ===== 公開路由（不需要登入）=====
                        .requestMatchers(HttpMethod.POST, "/api/members/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/members/login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/members/password/forgot").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/members/password/verify-code").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/members/password/reset").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/members/email/send-code").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/members/email/verify-code").permitAll()

                        // ===== 前台公開查詢 =====
                        .requestMatchers(HttpMethod.GET, "/api/homepage/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/stores/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/stores/nearby").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/menu-categories/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/menu-items/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/menu-component/stores/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/news/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/faqs/**").permitAll()

                        // ===== 前台訂位（允許未登入訂位）=====
                        .requestMatchers(HttpMethod.GET, "/api/reservations/slots/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/reservations").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/reservations/*/public").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/reservations/*").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/reservations/*").permitAll()
                        .requestMatchers(HttpMethod.PATCH, "/api/reservations/*/reserve").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/reservations/*").permitAll()

                        // ===== 前台訂單 / 付款（Demo 允許未登入操作）=====
                        .requestMatchers(HttpMethod.POST, "/api/orders").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/orders/**").permitAll()
                        .requestMatchers("/api/payments/**").permitAll()
                        .requestMatchers("/api/reservation-payments/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/favicon.ico").permitAll()

                        // ===== 個人資料路由（所有已登入角色都可以看 / 修改自己的資料）=====
                        .requestMatchers("/api/members/me/**")
                        .hasAnyAuthority("ROLE_CUSTOMER", "ROLE_STAFF", "ROLE_MANAGER", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/members/me")
                        .hasAnyAuthority("ROLE_CUSTOMER", "ROLE_STAFF", "ROLE_MANAGER", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/members/me")
                        .hasAnyAuthority("ROLE_CUSTOMER", "ROLE_STAFF", "ROLE_MANAGER", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/members/me")
                        .hasAnyAuthority("ROLE_CUSTOMER", "ROLE_STAFF", "ROLE_MANAGER", "ROLE_ADMIN")

                        // ===== 員工管理路由 =====
                        // 建立員工：只給 ADMIN
                        .requestMatchers(HttpMethod.POST, "/api/members/staff").hasAuthority("ROLE_ADMIN")
                        // 查詢員工清單：給 ADMIN、MANAGER
                        .requestMatchers(HttpMethod.GET, "/api/members/staff")
                        .hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER")
                        // 查詢 / 修改 / 刪除特定員工：給 ADMIN、MANAGER
                        .requestMatchers("/api/members/staff/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER")

                        // ===== 後台 API =====
                        // 門市與桌位管理：給 ADMIN、MANAGER
                        .requestMatchers("/api/admin/homepage/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/api/admin/stores/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER")
                        .requestMatchers("/api/admin/tables/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER")
                        .requestMatchers("/api/admin/news/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_MANAGER")
                        .requestMatchers("/api/admin/faqs/**").hasAuthority("ROLE_ADMIN")

                        // 後台訂位管理：給 STAFF、MANAGER、ADMIN
                        .requestMatchers("/api/admin/reservations/**")
                        .hasAnyAuthority("ROLE_STAFF", "ROLE_MANAGER", "ROLE_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/admin/reservation-settings/**")
                        .hasAnyAuthority("ROLE_STAFF", "ROLE_MANAGER", "ROLE_ADMIN")
                        .requestMatchers("/api/admin/reservation-settings/**")
                        .hasAnyAuthority("ROLE_MANAGER", "ROLE_ADMIN")

                        // 後台菜單管理：STAFF、MANAGER、ADMIN
                        .requestMatchers("/api/menu-items/**")
                        .hasAnyAuthority("ROLE_STAFF", "ROLE_MANAGER", "ROLE_ADMIN")

                        // 後台 Dashboard：營收與訂單統計，只給後台角色
                        .requestMatchers(HttpMethod.GET, "/api/admin/dashboard/**")
                        .hasAnyAuthority("ROLE_STAFF", "ROLE_MANAGER", "ROLE_ADMIN")

                        // 其他所有後台 API：給 STAFF、MANAGER、ADMIN
                        // 這條很重要，避免 CUSTOMER 直接用 Postman 打其他 /api/admin/** API
                        .requestMatchers("/api/admin/**").hasAnyAuthority("ROLE_STAFF", "ROLE_MANAGER", "ROLE_ADMIN")

                        // 其餘所有請求都需要登入
                        .anyRequest().authenticated())

                // 將 JwtAuthenticationFilter 插在 Spring Security 內建帳密驗證 Filter 之前
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * 密碼加密器，全系統統一使用 BCrypt。
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
