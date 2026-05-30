package com.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.seckill.dto.LoginDTO;
import com.seckill.dto.RegisterDTO;
import com.seckill.entity.User;
import com.seckill.exception.BusinessException;
import com.seckill.mapper.UserMapper;
import com.seckill.common.JwtUtils;
import com.seckill.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Override
    public void register(RegisterDTO dto) {
        // 1. 检查用户名是否已存在
        Long count = userMapper.selectCount(
            new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername())
        );
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }

        // 2. 密码加密
        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword())); // BCrypt!
        user.setPhone(dto.getPhone());
        user.setEmail(dto.getEmail());
        userMapper.insert(user);
    }

    @Override
    public String login(LoginDTO dto) {
        // 先留空，Step 5 再写
        return null;
    }
}