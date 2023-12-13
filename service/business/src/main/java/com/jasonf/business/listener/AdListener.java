package com.jasonf.business.listener;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.Map;

@Component
public class AdListener {
    @Resource
    private RestTemplate restTemplate;

    @RabbitListener(queues = {"ad_update_queue"})
    public void recvMsg(String position) {
        String url = "http://192.168.200.128/ad_update?position=" + position;
        restTemplate.getForObject(url, Map.class);
    }
}
