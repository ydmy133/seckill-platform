package com.seckill.entity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("seckill_product")
public class SeckillProduct {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long productId;
    private BigDecimal seckillPrice;
    private Integer stock;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer status;

    /** 乐观锁版本号 — 三层防超卖的第三层 */
    @Version
    private Integer version;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    // 以下字段不存在于数据库，用于关联查询展示
    @TableField(exist = false)
    private String productName;

    @TableField(exist = false)
    private BigDecimal originalPrice;

    @TableField(exist = false)
    private String imageUrl;
}