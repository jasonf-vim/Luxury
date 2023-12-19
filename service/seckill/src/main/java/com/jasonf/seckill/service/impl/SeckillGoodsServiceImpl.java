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

    private static final String SECKILL_GOODS_STOCK_KEY = "seckill:goods:stock:";

    @Resource
    private RedisTemplate redisTemplate;

    @Override
    @SuppressWarnings("unchecked")
    public List<SeckillGoods> list(String time) {   // time: 20231203
        List<SeckillGoods> goodsList = (List<SeckillGoods>) redisTemplate.opsForHash().values(SECKILL_GOODS_KEY + time);
        // 获取实时库存
        goodsList.forEach(goods -> {
            String stock = (String) redisTemplate.opsForValue().get(SECKILL_GOODS_STOCK_KEY + goods.getId());
            goods.setStockCount(Integer.parseInt(stock));
        });
        return goodsList;
    }
}
