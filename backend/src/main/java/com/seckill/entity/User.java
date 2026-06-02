package com.seckill.entity;          // 实体类都放在 entity 包下

import com.baomidou.mybatisplus.annotation.*; // 引入 MyBatis-Plus 的所有注解
import lombok.Data;                           // Lombok：自动生成 getter/setter/toString 等方法
import java.time.LocalDateTime;               // Java 8 的日期时间类，比老旧的 Date 更清晰

@Data                     // Lombok 注解：编译时自动生成 getXxx()、setXxx()、toString()、equals()、hashCode()
                          // 作用：省去手写几十行样板代码，让实体类只关注字段定义
@TableName("user")        // 告诉 MyBatis-Plus：这个实体类对应数据库的 user 表
public class User{        // 实体类 = 数据库表在 Java 中的映射，一个 User 对象 = user 表的一行数据

    @TableId(type = IdType.AUTO)  // 标记这是主键字段，IdType.AUTO = 由 MySQL 自增生成 ID（AUTO_INCREMENT）
    private Long id;              // 主键，Long 类型对应 MySQL 的 BIGINT

    private String username;      // 用户名
    private String password;      // 密码（存的是 BCrypt 加密后的密文，不是明文！）
    private String phone;         // 手机号
    private String email;         // 邮箱
    private String avatar;        // 头像 URL
    private Integer status;       // 状态：1=正常启用, 0=禁用

    @TableField(fill = FieldFill.INSERT)         // 插入数据时自动填充当前时间（需配合 MetaObjectHandler）
    private LocalDateTime createTime;            // 账号创建时间

    @TableField(fill = FieldFill.INSERT_UPDATE)  // 插入和更新数据时都自动填充当前时间
    private LocalDateTime updateTime;            // 最后修改时间
}
