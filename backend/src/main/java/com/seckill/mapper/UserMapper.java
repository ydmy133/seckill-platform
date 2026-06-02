package com.seckill.mapper;       // Mapper 接口都放在 mapper 包下

import com.baomidou.mybatisplus.core.mapper.BaseMapper;  // MyBatis-Plus 提供的基础 Mapper，内置 17 个通用方法
import com.seckill.entity.User;                          // 对应的实体类
import org.apache.ibatis.annotations.Mapper;             // MyBatis 的 Mapper 标记注解

@Mapper                        // 告诉 Spring：这是一个 MyBatis 的 Mapper 接口，请帮我管理它
public interface UserMapper extends BaseMapper<User> {   // extends BaseMapper<User> 是关键！
                                                         // 继承后自动获得：selectById, insert, updateById, deleteById,
                                                         // selectList, selectPage 等 17 个方法，不用写任何 SQL
}
