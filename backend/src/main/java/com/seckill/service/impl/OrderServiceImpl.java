package com.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.seckill.entity.Order;
import com.seckill.entity.Product;
import com.seckill.mapper.OrderMapper;
import com.seckill.mapper.ProductMapper;
import com.seckill.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;

    @Override
    public Page<Order> listUserOrders(Long userId, int page, int size) {
        Page<Order> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .orderByDesc(Order::getCreateTime);

        Page<Order> result = orderMapper.selectPage(pageParam, wrapper);
        for (Order order : result.getRecords()) {
            Product product = productMapper.selectById(order.getProductId());
            if (product != null) {
                order.setProductName(product.getName());
            }
        }
        return result;
    }

    @Override
    public Order getOrderById(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order != null) {
            Product product = productMapper.selectById(order.getProductId());
            if (product != null) {
                order.setProductName(product.getName());
            }
        }
        return order;
    }
}
