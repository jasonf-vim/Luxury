package com.jasonf.seckill.web.controller;

import com.jasonf.entity.Result;
import com.jasonf.entity.StatusCode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("wseckillorder")
public class SeckillOrderController {
    @GetMapping("add")
    public Result add(@RequestParam("time") String time, @RequestParam("id") String id) {
        return new Result(true, StatusCode.OK, "秒杀成功");
    }
}
