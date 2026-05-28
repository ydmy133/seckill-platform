-- ============================================================
-- Seckill Platform - 数据库初始化脚本
-- 使用方法: mysql -u root -p < init.sql
-- ============================================================

CREATE DATABASE IF NOT EXISTS seckill DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE seckill;

-- 用户表
CREATE TABLE `user` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `username`      VARCHAR(64)  NOT NULL COMMENT '用户名',
    `password`      VARCHAR(256) NOT NULL COMMENT 'BCrypt加密后的密码',
    `phone`         VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
    `email`         VARCHAR(128) DEFAULT NULL COMMENT '邮箱',
    `avatar`        VARCHAR(512) DEFAULT NULL COMMENT '头像URL',
    `status`        TINYINT      NOT NULL DEFAULT 1 COMMENT '1=正常, 0=禁用',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`),
    UNIQUE KEY `uk_phone`    (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 商品表
CREATE TABLE `product` (
    `id`             BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `name`           VARCHAR(256)  NOT NULL COMMENT '商品名称',
    `description`    TEXT          DEFAULT NULL COMMENT '商品描述',
    `image_url`      VARCHAR(512)  DEFAULT NULL COMMENT '主图URL',
    `original_price` DECIMAL(10,2) NOT NULL COMMENT '原价',
    `status`         TINYINT       NOT NULL DEFAULT 1 COMMENT '1=上架, 0=下架',
    `create_time`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品基础信息表';

-- 秒杀商品表 (一个商品可以参与多次秒杀活动)
CREATE TABLE `seckill_product` (
    `id`             BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `product_id`     BIGINT        NOT NULL COMMENT '关联商品ID',
    `seckill_price`  DECIMAL(10,2) NOT NULL COMMENT '秒杀价格',
    `stock`          INT           NOT NULL COMMENT '秒杀库存',
    `start_time`     DATETIME      NOT NULL COMMENT '秒杀开始时间',
    `end_time`       DATETIME      NOT NULL COMMENT '秒杀结束时间',
    `status`         TINYINT       NOT NULL DEFAULT 0 COMMENT '0=未开始, 1=进行中, 2=已结束',
    `version`        INT           NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    `create_time`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_product_id` (`product_id`),
    KEY `idx_start_time` (`start_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀商品表';

-- 秒杀订单表
CREATE TABLE `seckill_order` (
    `id`                 BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键',
    `order_no`           VARCHAR(64)   NOT NULL COMMENT '订单编号',
    `user_id`            BIGINT        NOT NULL COMMENT '用户ID',
    `seckill_product_id` BIGINT        NOT NULL COMMENT '秒杀商品ID',
    `product_id`         BIGINT        NOT NULL COMMENT '商品ID(冗余, 方便查询)',
    `seckill_price`      DECIMAL(10,2) NOT NULL COMMENT '秒杀价格快照',
    `status`             TINYINT       NOT NULL DEFAULT 0 COMMENT '0=未支付, 1=已支付, 2=已取消, 3=已退款',
    `create_time`        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_order_no` (`order_no`),
    UNIQUE KEY `uk_user_seckill` (`user_id`, `seckill_product_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_seckill_product_id` (`seckill_product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='秒杀订单表';
