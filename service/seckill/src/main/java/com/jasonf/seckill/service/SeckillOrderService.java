package com.jasonf.seckill.service;

public interface SeckillOrderService {
    /**
     * 秒杀下单
     *
     * @param time
     * @param username
     * @param id
     */
    boolean add(String time, Long id, String username);
}
