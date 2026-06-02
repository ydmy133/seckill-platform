package com.seckill.controller;

import com.seckill.entity.Product;
import com.seckill.entity.SeckillProduct;
import com.seckill.mapper.ProductMapper;
import com.seckill.mapper.SeckillProductMapper;
import com.seckill.vo.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductMapper productMapper;
    private final SeckillProductMapper seckillProductMapper;

    // ==================== 商品管理 ====================

    /** 新增商品 */
    @PostMapping("/products")
    public Result<Product> createProduct(@RequestBody Product product) {
        productMapper.insert(product);
        return Result.ok(product);
    }

    /** 更新商品 */
    @PutMapping("/products/{id}")
    public Result<Product> updateProduct(@PathVariable Long id, @RequestBody Product product) {
        product.setId(id);
        productMapper.updateById(product);
        return Result.ok(product);
    }

    /** 删除商品 */
    @DeleteMapping("/products/{id}")
    public Result<?> deleteProduct(@PathVariable Long id) {
        productMapper.deleteById(id);
        return Result.ok();
    }

    // ==================== 秒杀活动管理 ====================

    /** 新增秒杀活动 */
    @PostMapping("/seckill-products")
    public Result<SeckillProduct> createSeckillProduct(@RequestBody SeckillProduct seckillProduct) {
        seckillProductMapper.insert(seckillProduct);
        return Result.ok(seckillProduct);
    }

    /** 更新秒杀活动 */
    @PutMapping("/seckill-products/{id}")
    public Result<SeckillProduct> updateSeckillProduct(@PathVariable Long id, @RequestBody SeckillProduct seckillProduct) {
        seckillProduct.setId(id);
        seckillProductMapper.updateById(seckillProduct);
        return Result.ok(seckillProduct);
    }
}
