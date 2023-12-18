package com.jasonf.pay.feign;

import com.jasonf.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient(name = "pay")
public interface PayFeign {

    @GetMapping("/wxPay/nativePay")
    Result nativePay(@RequestParam("orderId") String orderId, @RequestParam("money") Integer money);

    @PutMapping("wxPay/close/{orderId}")
    Result<Map<String, String>> closeOrder(@PathVariable("orderId") String orderId);

    @GetMapping("wxPay/query{orderId}")
    Result<Map<String, String>> queryOrder(@PathVariable("orderId") String orderId);
}
