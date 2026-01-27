package com.pharmacy.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class GlobalCorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration cfg = new CorsConfiguration();
        // 生产域名 & 本地开发白名单
        cfg.addAllowedOrigin("https://www.szijea.xin");
        cfg.addAllowedOrigin("http://www.szijea.xin");
        cfg.addAllowedOrigin("http://localhost:8080");
        cfg.addAllowedOrigin("http://127.0.0.1:8080");
        // 若前端可能跑在 5173/Vite 等端口，可再添加：cfg.addAllowedOrigin("http://localhost:5173");
        cfg.setAllowCredentials(true);
        cfg.addAllowedHeader("*");
        cfg.addAllowedMethod("GET");
        cfg.addAllowedMethod("POST");
        cfg.addAllowedMethod("PUT");
        cfg.addAllowedMethod("DELETE");
        cfg.addAllowedMethod("PATCH");
        cfg.addAllowedMethod("OPTIONS");
        // 暴露头保证前端可读（可按需增减）
        cfg.addExposedHeader("Set-Cookie");
        cfg.addExposedHeader("X-Request-Id");
        cfg.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return new CorsFilter(source);
    }
}
