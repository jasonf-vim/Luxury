package com.jasonf.order.listener;

import com.jasonf.order.service.OrderService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class OrderTimeoutListener {
    @Resource
    private OrderService orderService;

    @RabbitListener(queues = {"queue.ordercreate"})
    public void recvMsg(String orderId) {
        orderService.closeOrder(orderId);
    }
}
