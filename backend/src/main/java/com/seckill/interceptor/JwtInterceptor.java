package com.seckill.interceptor;   // 拦截器放在 interceptor 包下

import com.seckill.common.JwtUtils;          // JWT 工具类，用于验证和解析 Token
import jakarta.servlet.http.HttpServletRequest;   // HTTP 请求对象（Jakarta EE 9+ 包名从 javax 变为 jakarta）
import jakarta.servlet.http.HttpServletResponse;  // HTTP 响应对象
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;        // 标记为 Spring 管理的组件
import org.springframework.web.servlet.HandlerInterceptor; // Spring MVC 拦截器接口

@Component                     // 标记为 Spring 组件，以便在 WebConfig 中注入
@RequiredArgsConstructor       // 自动生成构造函数，注入 JwtUtils
public class JwtInterceptor implements HandlerInterceptor {  // 实现拦截器接口，必须重写 preHandle 方法

    private final JwtUtils jwtUtils;  // JWT 工具类，由 Spring 自动注入

    @Override
    public boolean preHandle(HttpServletRequest request,   // 请求对象：可以读取 URL、Header、参数等
                             HttpServletResponse response, // 响应对象：可以设置状态码、写入错误信息
                             Object handler)               // handler：将要执行的 Controller 方法
            throws Exception {

        // 第1步：如果是 OPTIONS 预检请求（跨域请求浏览器会先发 OPTIONS 试探），直接放行
        // 什么是 OPTIONS？浏览器在发送跨域 POST/PUT 请求前，会先发 OPTIONS 询问服务器是否允许跨域
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) { // getMethod() 返回 "GET"/"POST"/"OPTIONS" 等
            return true;  // true = 放行，继续处理请求
        }

        // 第2步：从请求头中提取 Authorization 字段
        String authHeader = request.getHeader("Authorization"); // 前端 Axios 拦截器会设置这个头
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // 如果没有 Authorization 头，或者不是 "Bearer xxx" 格式
            response.setStatus(401);                             // HTTP 401 = Unauthorized（未登录）
            response.setContentType("application/json;charset=UTF-8"); // 设置返回内容类型为 JSON
            response.getWriter().write("{\"code\":401,\"message\":\"未登录\"}"); // 返回 JSON 错误信息给前端
            return false;  // false = 拦截，不继续处理请求（Controller 方法不会被执行）
        }

        // 第3步：提取 Token 字符串（去掉 "Bearer " 前缀，共 7 个字符）
        String token = authHeader.substring(7);  // 比如 "Bearer eyJhbGci..." → "eyJhbGci..."

        // 第4步：验证 Token 是否有效（签名是否正确、是否过期）
        if (!jwtUtils.validateToken(token)) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"code\":401,\"message\":\"Token已过期, 请重新登录\"}");
            return false;  // Token 无效或已过期，拦截
        }

        // 第5步：Token 验证通过，从 Token 中提取 userId
        Long userId = jwtUtils.getUserIdFromToken(token);   // 解析 JWT 的 sub 字段获取用户 ID
        request.setAttribute("userId", userId);             // 把 userId 存入 request 的属性，后面的 Controller 可以读取

        return true;  // 放行，继续执行 Controller 方法
    }
    // 总结：preHandle 返回 true → 放行；返回 false → 拦截（请求到此为止）
}
