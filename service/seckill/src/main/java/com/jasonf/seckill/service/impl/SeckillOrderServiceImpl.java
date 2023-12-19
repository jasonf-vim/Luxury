package com.jasonf.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.jasonf.seckill.config.ConfirmMessageSender;
import com.jasonf.seckill.config.RabbitMQConfig;
import com.jasonf.seckill.dao.SeckillOrderMapper;
import com.jasonf.seckill.pojo.SeckillGoods;
import com.jasonf.seckill.pojo.SeckillOrder;
import com.jasonf.seckill.service.SeckillOrderService;
import com.jasonf.utils.IdWorker;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {
    private static final String SECKILL_GOODS_KEY = "seckill:goods:";

    private static final String SECKILL_GOODS_STOCK_KEY = "seckill:goods:stock:";

    @Resource
    private IdWorker idWorker;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private ConfirmMessageSender confirmMessageSender;

    @Resource
    private SeckillOrderMapper seckillOrderMapper;

    @Override
    @SuppressWarnings("unchecked")
    public boolean add(String time, Long id, String username) {
        // 避免恶意刷单
        if (preventRepeat(username, id)) {
            return false;
        }
        // 放置重复购买
        SeckillOrder queryOrder = seckillOrderMapper.queryOrderInfo(username, id);
        if (queryOrder != null) {
            return false;
        }
        // 获取商品数据并检查库存
        SeckillGoods seckillGoods = (SeckillGoods) redisTemplate.opsForHash().get(SECKILL_GOODS_KEY + time, id);
        if (seckillGoods == null) {
            return false;   // 秒杀失败(时间错误 / id错误 / 卖空)
        }
        Integer stock = (Integer) redisTemplate.opsForValue().get(SECKILL_GOODS_STOCK_KEY + id);
        if (stock == null || stock <= 0) {
            return false;   // 卖空
        }
        // 预扣减库存
        Long decrement = redisTemplate.opsForValue().decrement(SECKILL_GOODS_STOCK_KEY + id);   // 原子操作
        if (decrement <= 0) {   // 删除商品信息和库存信息
            redisTemplate.opsForHash().delete(SECKILL_GOODS_KEY + time, id);
            redisTemplate.delete(SECKILL_GOODS_STOCK_KEY + id);
        }
        // 生成订单
        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setId(idWorker.nextId());
        seckillOrder.setSeckillId(id);
        seckillOrder.setMoney(seckillGoods.getCostPrice());     // 每人一件
        seckillOrder.setUserId(username);
        seckillOrder.setSellerId(seckillGoods.getSellerId());
        seckillOrder.setCreateTime(new Date());
        seckillOrder.setStatus("0");    // 未支付

        // 向MQ推送订单
        confirmMessageSender.send("", RabbitMQConfig.SECKILL_ORDER_QUEUE, JSON.toJSONString(seckillOrder));
        return true;
    }

    private boolean preventRepeat(String username, Long id) {
        String key = "seckill_user_" + username + "_id_" + id;
        Long increment = redisTemplate.opsForValue().increment(key);    // 原子
        if (increment == 1) {
            redisTemplate.expire(key, 2, TimeUnit.MINUTES);
            return false;
        }
        return true;
    }
}
