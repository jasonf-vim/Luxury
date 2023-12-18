package com.jasonf.pay.service.impl;

import com.github.wxpay.sdk.WXPay;
import com.jasonf.pay.service.WxPayService;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class WxPayServiceImpl implements WxPayService {
    private static final BigDecimal BEI_LV = new BigDecimal(100);

    @Resource
    private WXPay wxPay;

    @Value("${wxpay.notify_url}")
    private String notifyUrl;

    @Override
    public Map nativePay(String orderId, Integer money) {
        try {
            Map<String, String> reqData = new HashMap<>();
            reqData.put("body", "二奢交易支付");
            reqData.put("out_trade_no", orderId);
            BigDecimal totalMoney = new BigDecimal(money);
            BigDecimal multiply = totalMoney.multiply(BEI_LV);
            BigDecimal fee = multiply.setScale(0, BigDecimal.ROUND_UP);
            reqData.put("total_fee", String.valueOf(fee));
            reqData.put("spbill_create_ip", "192.168.3.1");
            reqData.put("notify_url", notifyUrl);  // 回调接口
            reqData.put("trade_type", "NATIVE");
            return reqData;
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    @Override
    public Map orderQuery(String orderId) {
        try {
            Map<String, String> reqData = new HashMap<>();
            reqData.put("out_trade_no", orderId);
            return wxPay.orderQuery(reqData);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
