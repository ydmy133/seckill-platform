package com.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seckill.entity.Product;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {
    // 继承 BaseMapper<Product>，自动拥有对 product 表的增删改查方法
    // 如果需要自定义 SQL（比如 JOIN 查询），可以在这里添加方法并在 XML 中写 SQL
}
