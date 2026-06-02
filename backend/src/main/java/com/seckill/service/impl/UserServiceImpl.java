package com.seckill.service.impl;  // Service 的实现类放在 service.impl 包下

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper; // 链式查询构造器（替代手写 SQL WHERE）
import com.seckill.dto.LoginDTO;
import com.seckill.dto.RegisterDTO;
import com.seckill.entity.User;
import com.seckill.exception.BusinessException;   // 业务异常（用户名已存在、密码错误等）
import com.seckill.mapper.UserMapper;
import com.seckill.common.JwtUtils;              // JWT 工具类（生成 token、验证 token）
import com.seckill.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;   // BCrypt 密码加密/验证
import org.springframework.stereotype.Service;

@Service                      // 标记为 Service 组件：Spring 会自动创建此类的单例对象并管理
@RequiredArgsConstructor      // 为所有 final 字段生成构造函数，Spring 通过这个构造函数注入依赖
public class UserServiceImpl implements UserService {   // implements = 实现 UserService 接口，必须重写其所有方法

    private final UserMapper userMapper;          // Mapper：操作数据库的 user 表
    private final PasswordEncoder passwordEncoder; // 密码加密器（BCryptPasswordEncoder）
    private final JwtUtils jwtUtils;               // JWT 工具（生成和解析 Token）

    @Override                               // 表示这个方法是重写接口中定义的方法
    public void register(RegisterDTO dto) { // dto 是从 Controller 传进来的注册信息

        // 第1步：检查用户名是否已存在（如果已存在就不能再注册）
        Long count = userMapper.selectCount(           // selectCount：查符合条件的记录数，相当于 SELECT COUNT(*)
            new LambdaQueryWrapper<User>()              // LambdaQueryWrapper：用 Java 方法引用代替手写 SQL 列名
                .eq(User::getUsername, dto.getUsername()) // .eq = equals，生成 WHERE username = ? 条件
                                                         // User::getUsername 是方法引用，编译期检查，不会拼错列名
        );
        if (count > 0) {                                // 如果查到了同名用户
            throw new BusinessException("用户名已存在");   // 抛业务异常，被 GlobalExceptionHandler 捕获后返回友好 JSON
        }

        // 第2步：创建用户对象并加密密码
        User user = new User();                                  // 新建一个空的 User 对象（对应 user 表的一行）
        user.setUsername(dto.getUsername());                     // 用户名直接存
        user.setPassword(passwordEncoder.encode(dto.getPassword())); // 重点：用 BCrypt 加密密码，不能存明文！
                                                                     // encode() 每次生成的结果不同（自动加随机盐），即使同一密码
        user.setPhone(dto.getPhone());                           // 手机号
        user.setEmail(dto.getEmail());                           // 邮箱
        userMapper.insert(user);                                 // 调用 MyBatis-Plus 的 insert 方法，执行 INSERT INTO user ...
    }

    @Override
    public String login(LoginDTO dto) {  // 登录：验证用户名和密码，成功返回 JWT

        // 第1步：根据用户名查用户
        User user = userMapper.selectOne(                     // selectOne：查一条记录，找不到返回 null
            new LambdaQueryWrapper<User>()
                .eq(User::getUsername, dto.getUsername())      // WHERE username = ?
        );
        if (user == null) {                                    // 如果没查到
            throw new BusinessException("用户名或密码错误");      // 模糊提示，不让攻击者知道用户名是否存在
        }

        // 第2步：验证密码
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            // matches(明文, 密文)：比对用户输入的明文密码和数据库中 BCrypt 密文是否匹配
            throw new BusinessException("用户名或密码错误");      // 同样模糊提示
        }

        // 第3步：生成 JWT Token
        return jwtUtils.generateToken(user.getId()); // 用用户 ID 生成 Token
                                                     // Token 里存了 userId，后续拦截器从中提取，无需重复查库
    }
}
