package com.jasonf.task.job;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class OrderTask {
    @Resource
    private RabbitTemplate rabbitTemplate;

    @Scheduled(cron = "0 0 1 * * *")    // 每天凌晨一点检查需要自动收货的订单
    public void autoTake() {
        rabbitTemplate.convertAndSend("", "order_tack", "take");
    }
}
