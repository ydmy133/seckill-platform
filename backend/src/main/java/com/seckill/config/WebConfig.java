package com.seckill.config;

import com.seckill.interceptor.JwtInterceptor;  // 刚才创建的 JWT 拦截器
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;     // 标记为配置类
import org.springframework.web.servlet.config.annotation.CorsRegistry;       // 跨域配置注册表
import org.springframework.web.servlet.config.annotation.InterceptorRegistry; // 拦截器注册表
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;   // Spring MVC 配置接口

@Configuration               // 标记为 Spring 配置类：启动时自动加载
@RequiredArgsConstructor     // 注入 JwtInterceptor
public class WebConfig implements WebMvcConfigurer {  // 实现这个接口可以自定义 Spring MVC 的配置

    private final JwtInterceptor jwtInterceptor;      // JWT 拦截器，由构造函数自动注入

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
    public void addInterceptors(InterceptorRegistry registry) {  // 重写拦截器配置方法
        registry.addInterceptor(jwtInterceptor)          // 注册 JWT 拦截器
                .addPathPatterns("/api/**")               // 拦截所有 /api/ 开头的请求（需要登录才能访问）
                .excludePathPatterns(                     // 排除不需要拦截的路径（不用登录也能访问）：
                        "/api/auth/login",                // 登录接口 — 还没登录哪有 Token
                        "/api/auth/register",             // 注册接口 — 新用户还没账号
                        "/api/products",                  // 商品列表 — 游客也可以浏览商品
                        "/api/products/*/seckill"         // 秒杀详情 — 游客也可以看
                );
    }
}
