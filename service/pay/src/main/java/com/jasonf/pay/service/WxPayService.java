package com.jasonf.pay.service;

import java.util.Map;

public interface WxPayService {
    /**
     * 微信下单二维码
     *
     * @param orderId
     * @param money
     * @return
     */
    Map nativePay(String orderId, Integer money);

    Map<String, String> orderQuery(String orderId);

    Map<String, String> closeOrder(String orderId);
}
