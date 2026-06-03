package com.seckill.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.seckill.entity.Order;
import com.seckill.service.OrderService;
import com.seckill.vo.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public Result<Page<Order>> list(HttpServletRequest request,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.ok(orderService.listUserOrders(userId, page, size));
    }

    @GetMapping("/{id}")
    public Result<Order> detail(@PathVariable Long id) {
        Order order = orderService.getOrderById(id);
        if (order == null) return Result.fail("订单不存在");
        return Result.ok(order);
    }
}
