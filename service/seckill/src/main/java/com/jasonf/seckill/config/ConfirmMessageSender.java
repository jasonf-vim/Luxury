package com.jasonf.seckill.config;

import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 消息代发器
 */

@Component
@SuppressWarnings("unchecked")
public class ConfirmMessageSender implements RabbitTemplate.ConfirmCallback {
    public static final String MESSAGE_CONFIRM_KEY = "message:confirm:";

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private RedisTemplate redisTemplate;

    public ConfirmMessageSender(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;   // 构造注入
        rabbitTemplate.setConfirmCallback(this);    // 回调通知到当前实例
    }

    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        if (ack) {  // 发送成功, 删除逻辑备份
            redisTemplate.delete(correlationData.getId());
            redisTemplate.delete(MESSAGE_CONFIRM_KEY + correlationData.getId());
        } else {    //发送失败, 重新发送
            Map<String, String> map = redisTemplate.opsForHash().entries(MESSAGE_CONFIRM_KEY + correlationData.getId());
            String exchange = map.get("exchange");
            String routingKey = map.get("routingKey");
            String message = map.get("message");
            rabbitTemplate.convertAndSend(exchange, routingKey, message);
        }
    }

    public void send(String exchange, String routingKey, String message) {
        CorrelationData correlationData = new CorrelationData(UUID.randomUUID().toString());

        // 逻辑备份到redis
        Map<String, String> map = new HashMap<>();
        map.put("exchange", exchange);
        map.put("routingKey", routingKey);
        map.put("message", message);
        redisTemplate.opsForHash().putAll(MESSAGE_CONFIRM_KEY + correlationData.getId(), map);

        rabbitTemplate.convertAndSend(exchange, routingKey, message, correlationData);
    }
}
