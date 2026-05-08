package com.mallcloud.api.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

/**
 * Spring Cloud Gateway 跨域配置类
 * <p>
 * 说明：
 * - 本配置使用 CorsWebFilter 实现全局跨域支持
 * - 适用于 Spring Cloud Gateway 2020.0.0 及以上版本（基于 Spring WebFlux）
 * - 该 Filter 会在请求进入路由之前生效，确保 OPTIONS 预检请求被正确处理
 */
@Configuration(proxyBeanMethods = false)
public class CorsConfig {

    /**
     * 创建 CorsWebFilter Bean，用于处理跨域请求
     * 
     * @return 配置好的 CorsWebFilter 实例
     */
    @Bean
    public CorsWebFilter corsWebFilter() {
        // 1. 创建 CorsConfiguration 对象，用于定义跨域规则
        CorsConfiguration corsConfiguration = new CorsConfiguration();

        // 2. 允许的请求来源（Origin）
        // 注意：生产环境不要使用 "*"，应明确指定可信域名
        corsConfiguration.addAllowedOriginPattern("*"); // Spring Boot 2.4+ 推荐使用 addAllowedOriginPattern 代替 addAllowedOrigin
        // 如果你使用的是较老版本（<2.4），请使用：corsConfiguration.addAllowedOrigin("*");

        // 3. 允许携带 Cookie（如果前端需要发送认证信息如 JWT Token）
        corsConfiguration.setAllowCredentials(true);

        // 4. 允许的 HTTP 方法
        corsConfiguration.addAllowedMethod("GET");
        corsConfiguration.addAllowedMethod("POST");
        corsConfiguration.addAllowedMethod("PUT");
        corsConfiguration.addAllowedMethod("DELETE");
        corsConfiguration.addAllowedMethod("OPTIONS");

        // 5. 允许的请求头（Header）
        // 常见的如 Content-Type, Authorization, X-Requested-With 等
        corsConfiguration.addAllowedHeader("*"); // 允许所有请求头（生产环境建议明确列出）

        // 6. 暴露给前端的响应头（可选）
        corsConfiguration.addExposedHeader("Authorization");
        corsConfiguration.addExposedHeader("Content-Disposition");

        // 7. 预检请求（OPTIONS）缓存时间（单位：秒）
        corsConfiguration.setMaxAge(3600L); // 1小时，减少预检请求次数

        // 8. 创建 UrlBasedCorsConfigurationSource，将 CORS 配置应用到所有路径
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", corsConfiguration); // /** 表示匹配所有路径

        // 9. 返回 CorsWebFilter，Spring Gateway 会自动将其加入过滤器链
        return new CorsWebFilter(source);
    }
}