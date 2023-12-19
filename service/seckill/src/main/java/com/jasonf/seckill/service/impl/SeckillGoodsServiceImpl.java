package com.jasonf.seckill.service.impl;

import com.jasonf.seckill.pojo.SeckillGoods;
import com.jasonf.seckill.service.SeckillGoodsService;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
public class SeckillGoodsServiceImpl implements SeckillGoodsService {
    private static final String SECKILL_GOODS_KEY = "seckill:goods:";

    @Resource
    private RedisTemplate redisTemplate;

    @Override
    public List<SeckillGoods> list(String time) {   // time: 20231203
        return (List<SeckillGoods>) redisTemplate.opsForHash().values(SECKILL_GOODS_KEY + time);
    }
}
