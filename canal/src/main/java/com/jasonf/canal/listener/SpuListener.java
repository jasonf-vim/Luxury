package com.jasonf.canal.listener;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.jasonf.canal.config.RabbitMQConfig;
import com.xpand.starter.canal.annotation.CanalEventListener;
import com.xpand.starter.canal.annotation.ListenPoint;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@CanalEventListener
public class SpuListener {
    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * spu表更新
     *
     * @param rowData
     */
    @ListenPoint(schema = "goods", table = {"tb_spu"}, eventType = CanalEntry.EventType.UPDATE)
    public void goodsUpdate(CanalEntry.RowData rowData) {
        // 修改前数据
        Map<String, String> oldMap = new HashMap<>();
        for (CanalEntry.Column column : rowData.getBeforeColumnsList()) {
            oldMap.put(column.getName(), column.getValue());
        }

        // 修改后数据
        Map<String, String> newMap = new HashMap<>();
        for (CanalEntry.Column column : rowData.getAfterColumnsList()) {
            newMap.put(column.getName(), column.getValue());
        }

        // focus on 'is_marketable'
        if ("0".equals(oldMap.get("is_marketable")) && "1".equals(newMap.get("is_marketable"))) {
            // 发送到对应交换机
            rabbitTemplate.convertAndSend(RabbitMQConfig.GOODS_UP_EXCHANGE, "", newMap.get("id"));
        }

        if ("1".equals(oldMap.get("is_marketable")) && "0".equals(newMap.get("is_marketable"))) {
            // 发送到对应交换机
            rabbitTemplate.convertAndSend(RabbitMQConfig.GOODS_DOWN_EXCHANGE, "", newMap.get("id"));
        }
    }
}
