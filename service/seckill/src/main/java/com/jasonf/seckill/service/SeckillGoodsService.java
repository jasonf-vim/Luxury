package com.jasonf.seckill.service;

import com.jasonf.seckill.pojo.SeckillGoods;

import java.util.List;

public interface SeckillGoodsService {
    /**
     * 检索某时段秒杀商品
     *
     * @param time
     * @return
     */
    List<SeckillGoods> list(String time);
}
