package com.seckill.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.seckill.entity.SeckillProduct;

public interface SeckillProductService {
    /** 查询当前可用的秒杀活动列表（进行中的） */
    Page<SeckillProduct> listActiveSeckillProducts(int page, int size);

    /** 根据 ID 查秒杀活动详情（含关联的商品名、原价等） */
    SeckillProduct getSeckillProductById(Long seckillProductId);

    /** 把秒杀商品库存预热到 Redis（秒杀开始前由定时任务调用） */
    void warmUpStockToRedis(Long seckillProductId);
}
