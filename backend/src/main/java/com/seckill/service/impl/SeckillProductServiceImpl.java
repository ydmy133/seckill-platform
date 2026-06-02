package com.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.seckill.entity.Product;
import com.seckill.entity.SeckillProduct;
import com.seckill.mapper.ProductMapper;
import com.seckill.mapper.SeckillProductMapper;
import com.seckill.service.SeckillProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class SeckillProductServiceImpl implements SeckillProductService {

    private final SeckillProductMapper seckillProductMapper;
    private final ProductMapper productMapper;
    private final StringRedisTemplate stringRedisTemplate;

    private static final String STOCK_KEY_PREFIX = "seckill:stock:";

    @Override
    public Page<SeckillProduct> listActiveSeckillProducts(int page, int size) {
        Page<SeckillProduct> pageParam = new Page<>(page, size);
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<SeckillProduct> wrapper = new LambdaQueryWrapper<SeckillProduct>()
                .eq(SeckillProduct::getStatus, 1)     // 进行中
                .le(SeckillProduct::getStartTime, now) // 已经开始
                .ge(SeckillProduct::getEndTime, now)   // 还未结束
                .orderByDesc(SeckillProduct::getCreateTime);

        Page<SeckillProduct> result = seckillProductMapper.selectPage(pageParam, wrapper);

        // 填充关联的商品信息（名称、原价、图片），因为 @TableField(exist=false) 不会自动查
        for (SeckillProduct sp : result.getRecords()) {
            Product product = productMapper.selectById(sp.getProductId());
            if (product != null) {
                sp.setProductName(product.getName());
                sp.setOriginalPrice(product.getOriginalPrice());
                sp.setImageUrl(product.getImageUrl());
            }
        }
        return result;
    }

    @Override
    public SeckillProduct getSeckillProductById(Long seckillProductId) {
        SeckillProduct sp = seckillProductMapper.selectById(seckillProductId);
        if (sp != null) {
            Product product = productMapper.selectById(sp.getProductId());
            if (product != null) {
                sp.setProductName(product.getName());
                sp.setOriginalPrice(product.getOriginalPrice());
                sp.setImageUrl(product.getImageUrl());
            }
        }
        return sp;
    }

    /** 把秒杀库存写入 Redis，设置过期时间为活动结束后 1 小时 */
    @Override
    public void warmUpStockToRedis(Long seckillProductId) {
        SeckillProduct sp = seckillProductMapper.selectById(seckillProductId);
        if (sp == null) return;

        String key = STOCK_KEY_PREFIX + seckillProductId;
        stringRedisTemplate.opsForValue().set(key, String.valueOf(sp.getStock()));

        // 计算距离活动结束还有多久，Redis key 在活动结束后自动过期
        long ttl = java.time.Duration.between(LocalDateTime.now(), sp.getEndTime()).toSeconds() + 3600;
        if (ttl > 0) {
            stringRedisTemplate.expire(key, ttl, TimeUnit.SECONDS);
        }
    }
}
