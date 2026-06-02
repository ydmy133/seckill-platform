package com.seckill.config;          // 配置类放在 config 包下

import org.springframework.context.annotation.Bean;                // @Bean 注解：标记方法返回的对象由 Spring 管理
import org.springframework.context.annotation.Configuration;        // @Configuration：告诉 Spring 这是配置类
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // BCrypt 加密算法实现
import org.springframework.security.crypto.password.PasswordEncoder;      // 密码加密器的接口（方便以后换算法）

@Configuration               // 标记这是配置类 = Spring 启动时会自动扫描并执行其中的 @Bean 方法
public class BeanConfig {     // 集中管理项目中需要"手动创建"的 Bean（Spring 无法自动推断的 Bean）

    @Bean                    // 告诉 Spring：把这个方法的返回值变成一个可注入的对象
    public PasswordEncoder passwordEncoder() {   // PasswordEncoder 是接口，BCryptPasswordEncoder 是具体实现
        return new BCryptPasswordEncoder();      // BCrypt：自动加盐，同一密码每次加密结果不同，防止彩虹表攻击
    }                                            // 以后其他 Service 需要加密密码时，直接注入 PasswordEncoder 即可
}
