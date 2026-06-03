package com.seckill.interceptor;

import com.seckill.common.RateLimited;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitInterceptor implements HandlerInterceptor {

    private final StringRedisTemplate stringRedisTemplate;
    private final DefaultRedisScript<Long> rateLimitScript;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        if (handler instanceof HandlerMethod hm) {
            RateLimited annotation = hm.getMethodAnnotation(RateLimited.class);
            if (annotation != null) {
                String key = "rate_limit:" + annotation.key();
                Long result = stringRedisTemplate.execute(
                        rateLimitScript,
                        List.of(key),
                        String.valueOf(annotation.capacity()),
                        String.valueOf(annotation.rate()),
                        String.valueOf(System.currentTimeMillis())
                );
                if (result == null || result == 0) {
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(429);
                    response.getWriter().write("{\"code\":429,\"message\":\"系统繁忙，请稍后再试\"}");
                    return false;
                }
            }
        }
        return true;
    }
}
