package com.seckill.controller;

import com.seckill.dto.LoginDTO;
import com.seckill.dto.RegisterDTO;
import com.seckill.service.UserService;
import com.seckill.vo.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public Result<?> register(@RequestBody RegisterDTO dto) {
        userService.register(dto);
        return Result.ok();
    }

    @PostMapping("/login")
    public Result<?> login(@RequestBody LoginDTO dto) {
        // Step 5 实现
        return null;
    }
}