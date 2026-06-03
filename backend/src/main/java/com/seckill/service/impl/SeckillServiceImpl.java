package com.seckill.service.impl;

import com.seckill.config.RabbitMQConfig;
import com.seckill.dto.SeckillMessage;
import com.seckill.service.SeckillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeckillServiceImpl implements SeckillService {

    private final StringRedisTemplate stringRedisTemplate;
    private final DefaultRedisScript<Long> seckillScript;
    private final RabbitTemplate rabbitTemplate;

    private static final String STOCK_KEY_PREFIX = "seckill:stock:";
    private static final String USERS_KEY_PREFIX = "seckill:users:";

    @Override
    public Long executeSeckill(Long seckillProductId, Long userId) {
        String stockKey = STOCK_KEY_PREFIX + seckillProductId;
        String usersKey = USERS_KEY_PREFIX + seckillProductId;

        Long result = stringRedisTemplate.execute(
                seckillScript,
                List.of(stockKey, usersKey),
                String.valueOf(userId)
        );

        if (result != null && result == 1) {
            SeckillMessage message = new SeckillMessage(seckillProductId, userId);
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    RabbitMQConfig.ROUTING_KEY,
                    message
            );
            log.info("Seckill success: userId={}, seckillProductId={}", userId, seckillProductId);
        }
        return result;
    }
}
