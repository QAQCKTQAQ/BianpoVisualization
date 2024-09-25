package com.fhzn.bianpovisualization.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")  // 允许所有请求路径
                .allowedOrigins("http://localhost:3000")  // 允许的前端源地址
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")  // 允许的 HTTP 方法
                .allowCredentials(true)  // 允许携带凭证
                .maxAge(3600);  // 预检请求的有效期
    }
}