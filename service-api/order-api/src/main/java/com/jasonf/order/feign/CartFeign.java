package com.jasonf.order.feign;

import com.jasonf.entity.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@FeignClient("order")
public interface CartFeign {
    @GetMapping("cart/list")
    Result<Map> list();

    @GetMapping("cart/addCart")
    Result addCart(@RequestParam("skuId") String skuId,
                   @RequestParam("num") Integer num);
}
