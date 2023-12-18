package com.jasonf.pay.feign;

import com.jasonf.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "pay")
public interface PayFeign {

    @GetMapping("/wxpay/nativePay")
    Result nativePay(@RequestParam("orderId") String orderId, @RequestParam("money") Integer money);
}
