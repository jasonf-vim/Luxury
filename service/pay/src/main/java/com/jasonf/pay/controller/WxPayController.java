package com.jasonf.pay.controller;

import com.alibaba.fastjson.JSON;
import com.github.wxpay.sdk.WXPayUtil;
import com.jasonf.entity.Result;
import com.jasonf.entity.StatusCode;
import com.jasonf.pay.config.RabbitMQConfig;
import com.jasonf.pay.service.WxPayService;
import com.jasonf.utils.ConvertUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("wxPay")
public class WxPayController {
    @Resource
    private WxPayService wxPayService;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @GetMapping("nativePay")
    public Result nativePay(@RequestParam("orderId") String orderId, @RequestParam("money") Integer money) {
        Map map = wxPayService.nativePay(orderId, money);
        return new Result(true, StatusCode.OK, "支付成功", map);
    }

    @GetMapping("notify")
    public void notifyMyself(HttpServletRequest request, HttpServletResponse response) {
        try {
            String xml = ConvertUtils.convertToString(request.getInputStream());
            Map<String, String> map = WXPayUtil.xmlToMap(xml);
            if ("SUCCESS".equals(map.get("return_code")) && "SUCCESS".equals(map.get("result_code"))) {
                String orderId = map.get("out_trade_no");
                String transactionId = map.get("transaction_id");
                Map<String, String> mqMap = new HashMap<>();
                mqMap.put("orderId", orderId);
                mqMap.put("transactionId", transactionId);
                // 向MQ发送信息
                rabbitTemplate.convertAndSend("", RabbitMQConfig.ORDER_PAY, JSON.toJSONString(mqMap));
                // 基于WebSocket回推用户
                rabbitTemplate.convertAndSend("paynotify", "", orderId);
            } else {
                System.out.println("出错，原因：" + map.get("return_msg"));
            }
            // 响应
            String rtn = "<xml><return_code><![CDATA[SUCCESS]]></return_code><return_msg><![CDATA[OK]]></return_msg></xml>";
            response.setContentType("text/html");
            response.getWriter().write(rtn);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
