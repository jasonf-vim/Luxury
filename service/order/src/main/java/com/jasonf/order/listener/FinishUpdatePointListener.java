package com.jasonf.order.listener;

import com.alibaba.fastjson.JSON;
import com.jasonf.order.config.RabbitMQConfig;
import com.jasonf.order.pojo.Task;
import com.jasonf.order.service.TaskService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class FinishUpdatePointListener {
    @Resource
    private TaskService taskService;

    @RabbitListener(queues = {RabbitMQConfig.CG_BUYING_FINISHADDPOINT})
    public void recvMsg(String json) {
        Task task = JSON.parseObject(json, Task.class);
        if (task != null) {
            taskService.pointTask(task);
        }
    }
}
