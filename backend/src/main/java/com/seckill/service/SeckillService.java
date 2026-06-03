package com.seckill.service;

public interface SeckillService {
    /** 执行秒杀，返回结果码：1=成功, -1=售罄, -2=重复 */
    Long executeSeckill(Long seckillProductId, Long userId);
}
