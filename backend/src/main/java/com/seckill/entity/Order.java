package com.seckill.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("seckill_order")     // 对应数据库的 seckill_order 表（秒杀订单）
public class Order {

    @TableId(type = IdType.AUTO)
    private Long id;              // 订单 ID，自增主键

    private String orderNo;       // 订单编号，如 "ORD20261231100001123456"，全局唯一（有唯一索引保护）
    private Long userId;          // 下单用户 ID，关联 user 表
    private Long seckillProductId; // 秒杀活动 ID，关联 seckill_product 表
    private Long productId;       // 商品 ID（冗余存储），方便直接查商品信息，避免每次都 JOIN
    private BigDecimal seckillPrice; // 秒杀价格快照：成交时的价格存入订单，即使后来商品改价也不影响历史订单
    private Integer status;       // 订单状态：0=未支付, 1=已支付, 2=已取消, 3=已退款

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;  // 下单时间

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;  // 最后状态变更时间

    @TableField(exist = false)   // 数据库中不存在此列，仅关联查询时使用
    private String productName;  // 商品名称（JOIN product 表得出）
}
