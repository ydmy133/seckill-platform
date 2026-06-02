package com.seckill;                // 声明包路径，这个文件在 com/seckill/ 目录下

import org.mybatis.spring.annotation.MapperScan;       // 引入 @MapperScan 注解，用于扫描 Mapper 接口
import org.springframework.boot.SpringApplication;      // Spring Boot 启动类
import org.springframework.boot.autoconfigure.SpringBootApplication; // 自动配置注解
import org.springframework.scheduling.annotation.EnableScheduling;   // 开启定时任务功能

@SpringBootApplication   // 核心注解 = @Configuration + @EnableAutoConfiguration + @ComponentScan
                         // 作用：1.标记这是启动类 2.自动扫描同包下的所有组件 3.根据 pom.xml 依赖自动配置
@MapperScan("com.seckill.mapper")  // 告诉 MyBatis-Plus 去哪找 Mapper 接口，扫描 com/seckill/mapper/ 下的所有接口
@EnableScheduling                  // 开启 Spring 的定时任务功能（Step 11 库存预热会用到 @Scheduled）
public class SeckillApplication {  // Spring Boot 项目的入口类，整个应用从这里启动

    public static void main(String[] args) {                   // Java 程序的入口方法，就像 Vue 的 main.js
        SpringApplication.run(SeckillApplication.class, args); // 启动 Spring Boot，自动加载所有配置和组件
    }
}
