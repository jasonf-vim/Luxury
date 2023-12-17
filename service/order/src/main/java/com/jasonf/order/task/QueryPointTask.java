package com.jasonf.order.task;

import com.alibaba.fastjson.JSON;
import com.jasonf.order.dao.TaskMapper;
import com.jasonf.order.pojo.Task;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@Component
public class QueryPointTask {
    @Resource
    private TaskMapper taskMapper;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Scheduled(cron = "* */1 * * * ?")
    public void queryPoint() {
        List<Task> taskList = taskMapper.findTasks(new Date());
        for (Task task : taskList) {
            rabbitTemplate.convertAndSend(task.getMqExchange(), task.getMqRoutingkey(), JSON.toJSONString(task));
        }
    }
}
