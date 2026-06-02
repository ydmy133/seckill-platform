package com.seckill.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seckill.entity.SeckillProduct;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SeckillProductMapper extends BaseMapper<SeckillProduct> {
    // 继承 BaseMapper<SeckillProduct>，自动拥有对 seckill_product 表的增删改查方法
}
