package com.jasonf.search.listener;

import com.jasonf.search.config.RabbitMQConfig;
import com.jasonf.search.service.ESManagerService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class GoodsUpListener {
    @Resource
    private ESManagerService esManagerService;

    @RabbitListener(queues = {RabbitMQConfig.SEARCH_ADD_QUEUE})
    public void recvMsg(String spuId) {
        esManagerService.importDataBySpuId(spuId);
    }
}
