package com.umc.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.divary.global.intercepter.LoggingInterceptor;

// Interceptor를 Spring MVC에 등록, 특정 URL 경로에 대해 적용
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final LoggingInterceptor loggingInterceptor;

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(loggingInterceptor)
                // 인터셉터가 실행될 경로를 설정하는 필터
                .addPathPatterns("/**")
                // 인터셉터가 실행되지 않을 경로를 설정하는 필터
                .excludePathPatterns("/h2-console/**", "/swagger-ui/**", "/v3/api-docs/**");
    }
} 