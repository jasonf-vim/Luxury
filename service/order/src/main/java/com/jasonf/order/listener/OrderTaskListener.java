package com.jasonf.order.listener;

import com.jasonf.order.service.OrderService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class OrderTaskListener {
    @Resource
    private OrderService orderService;

    @RabbitListener(queues = {"order_tack"})
    public void recvMsg() {
        orderService.autoTake();
    }
}
