package com.jasonf.consume.service;

import com.jasonf.seckill.pojo.SeckillOrder;

public interface SeckillOrderService {
    /**
     * 订单持久化
     *
     * @param seckillOrder
     * @return
     */
    boolean createOrder(SeckillOrder seckillOrder);
}
