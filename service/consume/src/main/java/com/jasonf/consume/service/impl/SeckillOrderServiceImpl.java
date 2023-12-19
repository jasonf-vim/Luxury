package com.jasonf.consume.service.impl;

import com.jasonf.consume.dao.SeckillGoodsMapper;
import com.jasonf.consume.dao.SeckillOrderMapper;
import com.jasonf.consume.service.SeckillOrderService;
import com.jasonf.seckill.pojo.SeckillOrder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
public class SeckillOrderServiceImpl implements SeckillOrderService {
    @Resource
    private SeckillGoodsMapper seckillGoodsMapper;

    @Resource
    private SeckillOrderMapper seckillOrderMapper;

    @Override
    @Transactional
    public boolean createOrder(SeckillOrder seckillOrder) {
        // goods表库存数量减一
        int rows = seckillGoodsMapper.updateStock(seckillOrder.getSeckillId());
        if (rows <= 0) {
            return false;
        }
        // 记录到订单表
        rows = seckillOrderMapper.insertSelective(seckillOrder);
        return rows > 0;
    }
}
