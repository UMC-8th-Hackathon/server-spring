package com.umc.auth.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/**",                 // 로그인 관련
                                "/swagger-ui/**",              // Swagger UI
                                "/v3/api-docs/**",             // OpenAPI 문서
                                "/swagger-resources/**",       // Swagger 리소스
                                "/webjars/**",                 // Swagger 자바스크립트 리소스
                                "/favicon.ico"
                        ).permitAll()
                        .anyRequest().authenticated()     // 나머지는 인증 필요
                );

        return http.build();
    }
}

