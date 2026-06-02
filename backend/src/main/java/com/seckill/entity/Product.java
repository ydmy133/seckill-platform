package com.seckill.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;      // 用于精确的金额计算，float/double 会有精度丢失问题
import java.time.LocalDateTime;

@Data
@TableName("product")             // 对应数据库的 product 表（商品基础信息）
public class Product {

    @TableId(type = IdType.AUTO)
    private Long id;              // 商品 ID，自增主键

    private String name;          // 商品名称，如 "iPhone 15 Pro"
    private String description;   // 商品描述，TEXT 类型，可以为空
    private String imageUrl;      // 商品主图的 URL 地址
    private BigDecimal originalPrice; // 商品原价，用 BigDecimal 保证金额精度（9999.99 不会变成 9999.98999...）
    private Integer status;       // 上架状态：1=上架可售, 0=下架

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
