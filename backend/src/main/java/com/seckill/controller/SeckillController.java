package com.seckill.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.seckill.common.RateLimited;
import com.seckill.entity.Order;
import com.seckill.mapper.OrderMapper;
import com.seckill.service.SeckillService;
import com.seckill.vo.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/seckill")
@RequiredArgsConstructor
@Slf4j
public class SeckillController {

    private final SeckillService seckillService;
    private final OrderMapper orderMapper;

    @RateLimited(key = "seckill", capacity = 100, rate = 10)
    @PostMapping("/{seckillProductId}/execute")
    public Result<String> execute(@PathVariable Long seckillProductId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        if (userId == null) {
            return Result.fail(401, "请先登录");
        }

        Long result = seckillService.executeSeckill(seckillProductId, userId);

        if (result == 1) {
            return Result.ok("抢购成功，订单处理中");
        } else if (result == -1) {
            return Result.fail("很遗憾，已售罄");
        } else if (result == -2) {
            return Result.fail("您已经抢过了");
        } else {
            return Result.fail("秒杀失败");
        }
    }

    @GetMapping("/{seckillProductId}/result")
    public Result<?> getResult(@PathVariable Long seckillProductId,
                               @RequestParam Long userId) {
        Order order = orderMapper.selectOne(new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .eq(Order::getSeckillProductId, seckillProductId));
        if (order != null) {
            return Result.ok(order);
        }
        return Result.ok(null);
    }
}
