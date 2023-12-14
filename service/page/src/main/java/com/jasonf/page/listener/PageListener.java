package com.jasonf.page.listener;

import com.jasonf.page.config.RabbitMQConfig;
import com.jasonf.page.service.PageService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class PageListener {
    @Resource
    private PageService pageService;

    @RabbitListener(queues = {RabbitMQConfig.PAGE_CREATE_QUEUE})
    public void recvMsg(String spuId) {
        pageService.generatePage(spuId);
    }
}
