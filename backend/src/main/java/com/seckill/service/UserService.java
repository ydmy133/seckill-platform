package com.seckill.service;

import com.seckill.dto.RegisterDTO;
import com.seckill.dto.LoginDTO;

public interface UserService {
    void register(RegisterDTO dto);
    String login(LoginDTO dto);
}