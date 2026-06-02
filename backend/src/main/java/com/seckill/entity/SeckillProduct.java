package com.seckill.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("seckill_product")    // 对应数据库的 seckill_product 表（秒杀活动配置）
public class SeckillProduct {

    @TableId(type = IdType.AUTO)
    private Long id;              // 秒杀活动 ID，自增主键

    private Long productId;       // 关联的商品 ID，指向 product 表的 id（外键关系）
    private BigDecimal seckillPrice; // 秒杀价格，必须低于原价
    private Integer stock;        // 秒杀库存数量，这是 Redis 库存预热的源头
    private LocalDateTime startTime; // 秒杀开始时间，在此之前用户只能看到倒计时
    private LocalDateTime endTime;   // 秒杀结束时间，之后按钮变灰
    private Integer status;       // 活动状态：0=未开始, 1=进行中, 2=已结束

    /** 乐观锁版本号 — 三层防超卖的第三层 */
    @Version                     // MyBatis-Plus 乐观锁注解：更新时自动检查 version，防止并发覆盖
    private Integer version;     // 比如 version=5，UPDATE 时 WHERE version=5，更新后 version 自动变成 6

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // ========== 以下字段不存在于数据库中，仅用于关联查询时展示 ==========
    // exist = false 意思是：MyBatis-Plus 生成 INSERT/UPDATE 时忽略这些字段

    @TableField(exist = false)   // 数据库中不存在此列
    private String productName;  // 从 product 表关联查出来的商品名称（JOIN 结果）

    @TableField(exist = false)
    private BigDecimal originalPrice; // 从 product 表关联查出来的原价

    @TableField(exist = false)
    private String imageUrl;     // 从 product 表关联查出来的图片 URL
}
