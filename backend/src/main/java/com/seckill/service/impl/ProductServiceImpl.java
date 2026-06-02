package com.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.seckill.entity.Product;
import com.seckill.mapper.ProductMapper;
import com.seckill.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;

    @Override
    public Page<Product> listProducts(int page, int size) {
        Page<Product> pageParam = new Page<>(page, size);
        // 只查状态=1（上架）的商品，按创建时间倒序
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .eq(Product::getStatus, 1)
                .orderByDesc(Product::getCreateTime);
        return productMapper.selectPage(pageParam, wrapper);
    }

    @Override
    public Product getProductById(Long id) {
        return productMapper.selectById(id);
    }
}
