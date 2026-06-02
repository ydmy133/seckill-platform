package com.seckill.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.seckill.entity.Product;
import com.seckill.entity.SeckillProduct;
import com.seckill.service.ProductService;
import com.seckill.service.SeckillProductService;
import com.seckill.vo.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final SeckillProductService seckillProductService;

    /** 商品列表（分页，游客可看） */
    @GetMapping
    public Result<Page<Product>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(productService.listProducts(page, size));
    }

    /** 单个商品详情 */
    @GetMapping("/{id}")
    public Result<Product> detail(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        if (product == null) {
            return Result.fail("商品不存在");
        }
        return Result.ok(product);
    }

    /** 秒杀活动列表（进行中的，游客可看） */
    @GetMapping("/seckill")
    public Result<Page<SeckillProduct>> seckillList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(seckillProductService.listActiveSeckillProducts(page, size));
    }

    /** 秒杀活动详情（含秒杀价格、库存、商品信息） */
    @GetMapping("/seckill/{seckillProductId}")
    public Result<SeckillProduct> seckillDetail(@PathVariable Long seckillProductId) {
        SeckillProduct sp = seckillProductService.getSeckillProductById(seckillProductId);
        if (sp == null) {
            return Result.fail("秒杀活动不存在");
        }
        return Result.ok(sp);
    }
}
