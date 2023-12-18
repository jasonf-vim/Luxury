package com.jasonf.order.listener;

import com.alibaba.fastjson.JSON;
import com.jasonf.order.config.RabbitMQConfig;
import com.jasonf.order.service.OrderService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

@Component
public class OrderPayListener {
    @Resource
    private OrderService orderService;

    @RabbitListener(queues = {RabbitMQConfig.ORDER_PAY})
    public void revMsg(String json) {
        Map<String, String> map = JSON.parseObject(json, Map.class);
        String orderId = map.get("orderId");
        String transactionId = map.get("transactionId");
        orderService.updatePayStatus(orderId, transactionId);
    }
}
