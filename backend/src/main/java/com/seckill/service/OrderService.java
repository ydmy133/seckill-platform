package com.seckill.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.seckill.entity.Order;

public interface OrderService {
    /** 分页查询用户的订单列表 */
    Page<Order> listUserOrders(Long userId, int page, int size);
    /** 查单个订单详情 */
    Order getOrderById(Long orderId);
}
