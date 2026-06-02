package com.seckill.dto;          // DTO = Data Transfer Object（数据传输对象），放在 dto 包

import lombok.Data;

@Data                               // 自动生成 getter/setter/toString 等方法
public class RegisterDTO {          // DTO 只定义"前端发给后端"的数据结构，不存数据库
    private String username;        // 前端 JSON: {"username":"abc","password":"123","phone":"","email":""}
    private String password;        // 明文密码（后端收到后用 BCrypt 加密再存库）
    private String phone;           // 可选，注册时可以不填
    private String email;           // 可选
}
