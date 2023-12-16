package com.jasonf.order.service;

import java.util.Map;

public interface CartService {
    /**
     * 加入购物车
     *
     * @param skuId
     * @param num
     * @param username
     */
    void addCart(String skuId, Integer num, String username);

    /**
     * 查询购物车列表
     *
     * @param username
     * @return
     */
    Map list(String username);
}
