package com.restaurant.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security 設定
 * 
 * ⚠️ 開發初期先全部放行，等會員模組做好登入功能後再改成 JWT 驗證
 * 會員組的同學：請在你的分支修改此檔案，加入 JWT Filter
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)  // 前後分離不需要 CSRF
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/**").permitAll()        // 開發期間全放行
                .requestMatchers("/swagger-ui/**").permitAll()  // Swagger 文件
                .requestMatchers("/api-docs/**").permitAll()
                .anyRequest().permitAll()
            );
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
