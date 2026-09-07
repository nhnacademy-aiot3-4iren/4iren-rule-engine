package com.nhnacademy.ruleengine.common.config;

import feign.RequestInterceptor;
import jakarta.servlet.ServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class FeignUserHeaderInterceptorConfig {
    private static final String X_USER_ID = "X-User-Id";
    private static final String X_USER_ROLE = "X-User-Role";

    @Bean
    public RequestInterceptor requestInterceptor(){
        return template -> {
            // 현재 요청 스레드에서 유저의 HTTP 요청 정보를 가져옴
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if(attributes != null){
                // 로그인된 유저의 X-User-Id와 X-User-Role 헤더를 추출
                String userId = attributes.getRequest().getHeader(X_USER_ID);
                String userRole = attributes.getRequest().getHeader(X_USER_ROLE);

                //값이 존재할 때만 FeignClient 요청 헤더에 그대로 실어줌
                if(userId != null) {
                    template.header(X_USER_ID, userId);
                }
                if(userRole != null) {
                    template.header(X_USER_ROLE, userRole);
                }
            }
        };
    }
}
