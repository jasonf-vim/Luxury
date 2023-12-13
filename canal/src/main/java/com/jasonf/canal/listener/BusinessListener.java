package com.jasonf.canal.listener;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.jasonf.canal.config.RabbitMQConfig;
import com.xpand.starter.canal.annotation.CanalEventListener;
import com.xpand.starter.canal.annotation.ListenPoint;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import javax.annotation.Resource;

@CanalEventListener
public class BusinessListener {
    @Resource
    private RabbitTemplate rabbitTemplate;

    @ListenPoint(schema = "business", table = {"tb_ad"})
    public void adUpdate(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {
        // 修改前数据
        for (CanalEntry.Column column : rowData.getBeforeColumnsList()) {
            if (column.getName().equals("position")) {
                System.out.println("发送消息到mq  ad_update_queue:" + column.getValue());
                // 发送到MQ，采用默认交换机
                rabbitTemplate.convertAndSend("", RabbitMQConfig.AD_UPDATE_QUEUE, column.getValue());
                break;
            }
        }

        // 修改后数据
        for (CanalEntry.Column column : rowData.getAfterColumnsList()) {
            if (column.getName().equals("position")) {
                System.out.println("发送消息到mq  ad_update_queue:" + column.getValue());
                rabbitTemplate.convertAndSend("", RabbitMQConfig.AD_UPDATE_QUEUE, column.getValue());
                break;
            }
        }
    }
}
