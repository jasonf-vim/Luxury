package com.jasonf.consume.listener;

import com.alibaba.fastjson.JSON;
import com.jasonf.consume.config.RabbitMQConfig;
import com.jasonf.consume.service.SeckillOrderService;
import com.jasonf.seckill.pojo.SeckillOrder;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

@Component
public class SeckillOrderListener {
    @Resource
    private SeckillOrderService seckillOrderService;

    @RabbitListener(queues = {RabbitMQConfig.SECKILL_ORDER_QUEUE})
    public void recvMsg(Message message, Channel channel) {
        try {
            channel.basicQos(100);  // 削峰
        } catch (IOException e) {
            e.printStackTrace();
        }
        SeckillOrder seckillOrder = JSON.parseObject(message.getBody(), SeckillOrder.class);
        boolean flag = seckillOrderService.createOrder(seckillOrder);
        try {
            if (flag) {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
            } else {
                channel.basicNack(message.getMessageProperties().getDeliveryTag(), false, true);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
