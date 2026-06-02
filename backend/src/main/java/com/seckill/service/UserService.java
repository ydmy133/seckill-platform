package com.seckill.service;       // Service 接口定义在 service 包

import com.seckill.dto.RegisterDTO;  // 注册需要的 DTO
import com.seckill.dto.LoginDTO;     // 登录需要的 DTO

public interface UserService {       // 接口 = 只声明方法签名，不写实现（就像 TypeScript 的 interface）
                                     // 为什么要分开？1.定义契约 2.方便测试时替换实现 3.Spring 可以自动切换实现类

    void register(RegisterDTO dto);  // 注册方法：传入注册信息，没有返回值（成功无返回，失败抛异常）

    String login(LoginDTO dto);      // 登录方法：传入用户名密码，返回 JWT Token 字符串
}
