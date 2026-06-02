package com.seckill.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.seckill.entity.Product;

public interface ProductService {
    /** 分页查询上架商品列表（游客和登录用户都能看） */
    Page<Product> listProducts(int page, int size);

    /** 根据 ID 查单个商品 */
    Product getProductById(Long id);
}
