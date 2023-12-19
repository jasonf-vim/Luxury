package com.jasonf.seckill.feign;

import com.jasonf.entity.Result;
import com.jasonf.seckill.pojo.SeckillGoods;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("seckill")
public interface SeckillFeign {
    @GetMapping("/seckillgoods/list")
    Result<List<SeckillGoods>> list(@RequestParam("time") String time);
}
