package com.neighbor.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {
    
    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();
        
        // 允许的来源
        config.addAllowedOrigin("http://localhost:5173");
        config.addAllowedOrigin("http://127.0.0.1:5173");
        config.addAllowedOrigin("http://localhost:5174");
        config.addAllowedOrigin("http://127.0.0.1:5174");
        
        // 允许的方法
        config.addAllowedMethod("*");
        
        // 允许的头
        config.addAllowedHeader("*");
        
        // 允许携带凭证（如 Cookie、Token）
        config.setAllowCredentials(true);
        
        // 预检请求的缓存时间
        config.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        
        return new CorsFilter(source);
    }
}
