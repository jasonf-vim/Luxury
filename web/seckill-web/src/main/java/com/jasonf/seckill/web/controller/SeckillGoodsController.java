package com.jasonf.seckill.web.controller;

import com.jasonf.entity.Result;
import com.jasonf.seckill.feign.SeckillFeign;
import com.jasonf.utils.DateUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("wseckillgoods")
public class SeckillGoodsController {
    @Resource
    private SimpleDateFormat simpleDateFormat;

    @Resource
    private SeckillFeign seckillFeign;

    @GetMapping("toIndex")
    public String toIndex() {
        return "seckill-index";
    }

    @GetMapping("timeMenus")
    @ResponseBody
    public List<String> timeMenus() {
        List<Date> dates = DateUtil.getDateMenus();
        return dates.stream().map(date -> simpleDateFormat.format(date)).collect(Collectors.toList());
    }

    @GetMapping("list")
    @ResponseBody
    public Result list(@RequestParam("time") String time) {
        String timeStr = DateUtil.formatStr(time);  // 时间格式转换
        return seckillFeign.list(time);
    }
}
