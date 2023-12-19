package com.jasonf.seckill.controller;

import com.jasonf.seckill.config.TokenDecode;
import com.jasonf.seckill.service.SeckillOrderService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("seckillorder")
public class SeckillOrderController {
    @Resource
    private TokenDecode tokenDecode;

    @Resource
    private SeckillOrderService seckillOrderService;

    // 秒杀下单
    @PostMapping("add")
    public boolean add(@RequestParam("time") String time, @RequestParam("id") Long id) {
        String username = tokenDecode.getUserInfo().get("username");
        return seckillOrderService.add(time, id, username);
    }
}
