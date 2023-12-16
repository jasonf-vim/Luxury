package com.jasonf.order.feign;

import com.jasonf.entity.Result;
import com.jasonf.order.pojo.Order;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("order")
public interface OrderFeign {
    @PostMapping("order")
    Result add(@RequestBody Order order);
}
