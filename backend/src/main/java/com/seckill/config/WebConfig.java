package com.seckill.config;

import com.seckill.interceptor.JwtInterceptor;
import com.seckill.interceptor.RateLimitInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final JwtInterceptor jwtInterceptor;
    private final RateLimitInterceptor rateLimitInterceptor;

    // ==================== 跨域配置（CORS）====================
    @Override
    public void addCorsMappings(CorsRegistry registry) {   // 重写跨域配置方法
        registry.addMapping("/**")                         // 对所有请求路径生效（/** = 任意路径）
                .allowedOriginPatterns("*")                 // 允许所有来源域名（开发阶段这样写，生产环境要限制）
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")  // 允许的 HTTP 方法
                .allowedHeaders("*")                        // 允许所有请求头（包括 Authorization）
                .allowCredentials(true);                    // 允许携带 Cookie（前端 Axios 的 withCredentials 需要）
    }                                                        // 总结：这段配置让前端（localhost:3000）可以调用后端 API

    // ==================== 拦截器配置 ====================
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 先 JWT 验证身份
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/login",
                        "/api/auth/register",
                        "/api/products/**"
                );

        // 限流拦截器只拦截秒杀接口（在 JWT 之后执行）
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/seckill/**");
    }
}
