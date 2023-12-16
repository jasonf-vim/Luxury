package com.jasonf.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.jasonf.goods.feign.SkuFeign;
import com.jasonf.goods.feign.SpuFeign;
import com.jasonf.goods.pojo.Sku;
import com.jasonf.goods.pojo.Spu;
import com.jasonf.order.pojo.OrderItem;
import com.jasonf.order.service.CartService;
import com.jasonf.utils.IdWorker;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CartServiceImpl implements CartService {
    private static final String CART = "cart:";

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private SkuFeign skuFeign;

    @Resource
    private SpuFeign spuFeign;

    @Resource
    private IdWorker idWorker;

    @Override
    public void addCart(String skuId, Integer num, String username) {
        OrderItem orderItem = JSON.parseObject((String) stringRedisTemplate.opsForHash().get(CART + username, skuId), OrderItem.class);
        if (orderItem != null) {    // 更新数据
            Integer weight = orderItem.getWeight() / orderItem.getNum();
            orderItem.setNum(orderItem.getNum() + num);
            if (orderItem.getNum() < 1) {
                stringRedisTemplate.opsForHash().delete(CART + username, skuId);
                return;
            }
            orderItem.setMoney(orderItem.getNum() * orderItem.getPrice());
            orderItem.setPayMoney(orderItem.getNum() * orderItem.getPrice());
            orderItem.setWeight(weight * orderItem.getNum());
        } else {
            Sku sku = skuFeign.find(skuId);
            Spu spu = spuFeign.findSpuById(sku.getSpuId());
            orderItem = sku2OrderItem(spu, sku, num);   // 构建新数据
        }
        stringRedisTemplate.opsForHash().put(CART + username, skuId, JSON.toJSONString(orderItem));
    }

    @Override
    public Map list(String username) {
        List<Object> values = stringRedisTemplate.opsForHash().values(CART + username);
        Integer totalNum = 0;
        Integer totalMoney = 0;
        List<OrderItem> orderItems = new ArrayList<>();
        for (Object value : values) {
            OrderItem orderItem = JSON.parseObject((String) value, OrderItem.class);
            totalNum += orderItem.getNum();
            totalMoney += orderItem.getMoney();
            orderItems.add(orderItem);
        }
        Map map = new HashMap();
        map.put("orderItems", orderItems);
        map.put("totalNum", totalNum);
        map.put("totalMoney", totalMoney);
        return map;
    }

    private OrderItem sku2OrderItem(Spu spu, Sku sku, Integer num) {
        OrderItem orderItem = new OrderItem();
        orderItem.setId(idWorker.nextId() + "");
        orderItem.setCategoryId1(spu.getCategory1Id());
        orderItem.setCategoryId2(spu.getCategory2Id());
        orderItem.setCategoryId3(spu.getCategory3Id());
        orderItem.setSpuId(spu.getId());
        orderItem.setSkuId(sku.getId());
        orderItem.setName(sku.getName());
        orderItem.setPrice(sku.getPrice());
        orderItem.setNum(num);
        orderItem.setMoney(sku.getPrice() * num);
        orderItem.setPayMoney(sku.getPrice() * num);
        orderItem.setImage(sku.getImage());
        orderItem.setWeight(sku.getWeight() * num);
        return orderItem;
    }
}
