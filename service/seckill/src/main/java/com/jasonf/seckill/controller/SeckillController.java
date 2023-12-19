package com.jasonf.seckill.controller;

import com.jasonf.entity.Result;
import com.jasonf.entity.StatusCode;
import com.jasonf.seckill.pojo.SeckillGoods;
import com.jasonf.seckill.service.SeckillGoodsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("seckillgoods")
public class SeckillController {
    @Resource
    private SeckillGoodsService seckillGoodsService;

    @GetMapping("list")
    public Result<List<SeckillGoods>> list(@RequestParam("time") String time) {
        List<SeckillGoods> seckillGoods = seckillGoodsService.list(time);
        return new Result<>(true, StatusCode.OK, "查询秒杀商品成功", seckillGoods);
    }
}
