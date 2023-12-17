package com.jasonf.user.listener;

import com.alibaba.fastjson.JSON;
import com.jasonf.order.pojo.Task;
import com.jasonf.user.config.RabbitMQConfig;
import com.jasonf.user.dao.PointLogMapper;
import com.jasonf.user.pojo.PointLog;
import com.jasonf.user.service.UserService;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
public class AddPointListener {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private PointLogMapper pointLogMapper;

    @Resource
    private UserService userService;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = {RabbitMQConfig.CG_BUYING_ADDPOINT})
    public void recvMsg(String json) {
        Task task = JSON.parseObject(json, Task.class);
        if (task == null || StringUtils.isEmpty(task.getRequestBody())) {
            return;
        }
        // 检查redis (锁)
        String value = stringRedisTemplate.opsForValue().get(String.valueOf(task.getId()));
        if (StringUtils.isNotEmpty(value)) {
            return;     // 任务正在进行
        }
        // 检查任务完成表
        Map request = JSON.parseObject(task.getRequestBody(), Map.class);
        PointLog pointLog = pointLogMapper.selectByPrimaryKey(request.get("order_id"));
        if (pointLog != null) {
            return;     // 任务已完成
        }
        // 加锁
        stringRedisTemplate.opsForValue().set(String.valueOf(task.getId()), json, 1, TimeUnit.MINUTES);
        userService.updatePoint(task);      // 更新积分, 记录日志
        // 释放锁
        stringRedisTemplate.delete(String.valueOf(task.getId()));

        // 返回通知
        rabbitTemplate.convertAndSend(RabbitMQConfig.EX_BUYING_ADDPOINTUSER, RabbitMQConfig.CG_BUYING_FINISHADDPOINT_KEY, json);
    }
}
