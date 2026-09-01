package com.nhnacademy.ruleengine.common.config;

import com.nhnacademy.ruleengine.common.interceptor.RoomManagementAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    private final RoomManagementAuthInterceptor roomManagementAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(roomManagementAuthInterceptor)
                .addPathPatterns("/api/rule/rooms/{room-id}/**");//인터셉터 검증이 필요한 API 경로 패턴 지정
    }
}
