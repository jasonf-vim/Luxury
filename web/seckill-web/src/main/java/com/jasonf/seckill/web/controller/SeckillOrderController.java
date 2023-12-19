package com.jasonf.seckill.web.controller;

import com.jasonf.entity.Result;
import com.jasonf.entity.StatusCode;
import com.jasonf.seckill.feign.SeckillFeign;
import com.jasonf.seckill.web.annotation.AccessLimit;
import com.jasonf.utils.CookieUtil;
import com.jasonf.utils.DateUtil;
import com.jasonf.utils.RandomUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("wseckillorder")
public class SeckillOrderController {
    @Resource
    private SeckillFeign seckillFeign;

    @Resource
    private RedisTemplate redisTemplate;

    @GetMapping("add")
    @AccessLimit
    public Result add(@RequestParam("time") String time, @RequestParam("id") String id,
                      @RequestParam(value = "random", required = false) String randomCode,
                      HttpServletRequest request) {
        // 校验请求
        if (StringUtils.isBlank(randomCode)) {
            return new Result(false, StatusCode.REMOTE_ERROR, "非法请求");
        }
        String jti = CookieUtil.readCookie(request, "uid").get("uid");
        String queryCode = (String) redisTemplate.opsForValue().get("random_code_" + jti);
        if (!randomCode.equals(queryCode)) {
            return new Result(false, StatusCode.REMOTE_ERROR, "非法请求");
        }
        redisTemplate.delete("random_code_" + jti);     // 立即清除
        // 下单业务
        if (seckillFeign.add(DateUtil.formatStr(time), Long.parseLong(id))) {
            return new Result(true, StatusCode.OK, "秒杀成功");
        } else {
            return new Result(true, StatusCode.ERROR, "秒杀失败");
        }
    }

    @GetMapping("getToken")
    public String getToken(HttpServletRequest request) {
        String randomString = RandomUtil.getRandomString();
        String jti = CookieUtil.readCookie(request, "uid").get("uid");
        // 10s 内访问有效
        redisTemplate.opsForValue().set("random_code_" + jti, randomString, 10, TimeUnit.SECONDS);
        return randomString;
    }
}
