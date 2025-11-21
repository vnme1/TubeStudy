package com.tubestudy.tracker.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS(Cross-Origin Resource Sharing) 설정 클래스
 * 개발 환경에서 Chrome Extension과 로컬 요청을 허용합니다.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 모든 /api/** 경로에 CORS 허용
        registry.addMapping("/api/**")
                // Chrome Extension과 localhost 모두 허용
                .allowedOrigins("http://localhost:*", "http://127.0.0.1:*", "chrome-extension://*")
                // 필요한 HTTP 메서드 허용
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                // 모든 헤더 허용
                .allowedHeaders("*")
                // Content-Type 같은 커스텀 헤더도 허용
                .exposedHeaders("Content-Type")
                // 클라이언트가 쿠키 전송 가능
                .allowCredentials(false)
                // preflight 요청 캐시 시간 (초)
                .maxAge(3600);
    }
}