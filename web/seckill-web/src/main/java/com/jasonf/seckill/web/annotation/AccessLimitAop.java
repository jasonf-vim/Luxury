package com.jasonf.seckill.web.annotation;

import com.alibaba.fastjson.JSON;
import com.google.common.util.concurrent.RateLimiter;
import com.jasonf.entity.Result;
import com.jasonf.entity.StatusCode;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Aspect
@Component
public class AccessLimitAop {
    private RateLimiter rateLimiter = RateLimiter.create(5.0);  // 每秒放入5个令牌

    @Resource
    private HttpServletResponse response;

    // 声明切点
    @Pointcut("@annotation(com.jasonf.seckill.web.annotation.AccessLimit)")
    public void limit() {
    }

    // 环绕增强
    @Around("limit()")
    public Object around(ProceedingJoinPoint joinPoint) {
        boolean flag = rateLimiter.tryAcquire();
        Object obj = null;
        try {
            if (flag) {
                obj = joinPoint.proceed();
            } else {
                Result result = new Result(false, StatusCode.ACCESS_ERROR, "请稍后重试");
                String msg = JSON.toJSONString(result);
                writeMsg(response, msg);
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
        return obj;
    }

    private void writeMsg(HttpServletResponse response, String msg) {
        try (ServletOutputStream outputStream = response.getOutputStream()) {
            response.setContentType("application/json;charset=utf-8");
            outputStream.write(msg.getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
