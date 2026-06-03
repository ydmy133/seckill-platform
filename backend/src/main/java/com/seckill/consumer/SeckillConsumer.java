package com.seckill.consumer;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.seckill.config.RabbitMQConfig;
import com.seckill.dto.SeckillMessage;
import com.seckill.entity.Order;
import com.seckill.entity.SeckillProduct;
import com.seckill.mapper.OrderMapper;
import com.seckill.mapper.SeckillProductMapper;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeckillConsumer {

    private final OrderMapper orderMapper;
    private final SeckillProductMapper seckillProductMapper;
    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME)
    public void handleSeckillOrder(Message message, Channel channel) {
        SeckillMessage msg = (SeckillMessage) rabbitTemplate.getMessageConverter()
                .fromMessage(message);

        try {
            SeckillProduct sp = seckillProductMapper.selectById(msg.getSeckillProductId());
            if (sp == null) {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                return;
            }

            LambdaUpdateWrapper<SeckillProduct> wrapper = new LambdaUpdateWrapper<SeckillProduct>()
                    .eq(SeckillProduct::getId, sp.getId())
                    .eq(SeckillProduct::getVersion, sp.getVersion())
                    .gt(SeckillProduct::getStock, 0)
                    .set(SeckillProduct::getStock, sp.getStock() - 1)
                    .set(SeckillProduct::getVersion, sp.getVersion() + 1);

            int updated = seckillProductMapper.update(null, wrapper);
            if (updated == 0) {
                log.warn("Optimistic lock failed or stock exhausted for {}", msg.getSeckillProductId());
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                return;
            }

            Order order = new Order();
            order.setOrderNo(generateOrderNo(msg.getUserId()));
            order.setUserId(msg.getUserId());
            order.setSeckillProductId(msg.getSeckillProductId());
            order.setProductId(sp.getProductId());
            order.setSeckillPrice(sp.getSeckillPrice());
            order.setStatus(0);

            try {
                orderMapper.insert(order);
            } catch (DuplicateKeyException e) {
                log.warn("Duplicate order prevented: userId={}, spId={}",
                        msg.getUserId(), msg.getSeckillProductId());
            }

            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            log.error("Order creation failed: {}", e.getMessage());
            try {
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
            } catch (Exception ex) {
                log.error("NACK failed", ex);
            }
        }
    }

    private String generateOrderNo(Long userId) {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return "ORD" + now + String.format("%04d", userId % 10000);
    }
}
