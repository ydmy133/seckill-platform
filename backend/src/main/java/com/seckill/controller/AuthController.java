package com.seckill.controller;     // Controller 是前后端的"接口层"，每一个方法 = 一个 API 端点

import com.seckill.dto.LoginDTO;       // 登录请求的 DTO
import com.seckill.dto.RegisterDTO;    // 注册请求的 DTO
import com.seckill.service.UserService; // 用户业务逻辑层（Controller 不直接操作数据库，只调用 Service）
import com.seckill.vo.Result;          // 统一响应格式
import lombok.RequiredArgsConstructor;  // Lombok：为 final 字段自动生成构造函数
import org.springframework.web.bind.annotation.*;  // 所有 Web 注解

@RestController              // = @Controller + @ResponseBody：所有方法的返回值自动转为 JSON 字符串
@RequestMapping("/api/auth") // 这个控制器下所有接口的路径前缀是 /api/auth
@RequiredArgsConstructor     // Lombok：自动生成构造函数，Spring 通过这个构造函数注入 userService
public class AuthController {

    private final UserService userService;  // final 字段 + @RequiredArgsConstructor = 构造函数注入
                                            // Spring 会自动找到 UserService 的实现类并传入

    @PostMapping("/register")              // 处理 POST /api/auth/register 请求
    public Result<?> register(@RequestBody RegisterDTO dto) {   // @RequestBody：把前端发来的 JSON 字符串
        userService.register(dto);          // 调用业务层处理注册    自动转换成 RegisterDTO 对象
        return Result.ok();                 // 返回: {"code":200,"message":"success","data":null}
    }

    @PostMapping("/login")                 // 处理 POST /api/auth/login 请求（Step 5 完成登录逻辑）
    public Result<String> login(@RequestBody LoginDTO dto) {
        String token = userService.login(dto); // 调用登录逻辑，返回 JWT 字符串
        return Result.ok(token);               // 返回: {"code":200,"message":"success","data":"eyJhbGciOi..."}
    }
}
