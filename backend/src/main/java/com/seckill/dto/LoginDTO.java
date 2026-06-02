package com.seckill.dto;          // 声明这个类属于 com.seckill.dto 包（文件夹）

import lombok.Data;                 // 引入 Lombok 的 @Data 注解

@Data                               // Lombok注解：自动生成 getter/setter/toString/equals/hashCode 方法
public class LoginDTO {             // DTO = Data Transfer Object，前端发来的 JSON 数据的结构定义
    private String username;        // 用户名：前端 JSON 中的 "username" 字段，类型为字符串
    private String password;        // 密码：前端 JSON 中的 "password" 字段，类型为字符串
}
