package com.jasonf;

import com.jasonf.interceptor.FeignInterceptor;
import com.jasonf.order.config.TokenDecode;
import com.jasonf.utils.IdWorker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@EnableEurekaClient
@MapperScan(basePackages = {"com.jasonf.order.dao"})
@EnableFeignClients(basePackages = {"com.jasonf.goods.feign", "com.jasonf.pay.feign"})
@EnableScheduling   // 开启定时任务
public class OrderApplication {
    @Value("${workerId}")
    Long workerId;

    @Value("${dataCenterId}")
    Long dataCenterId;

    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class);
    }

    @Bean
    public IdWorker idWorker() {
        return new IdWorker(workerId, dataCenterId);
    }

    @Bean
    public TokenDecode tokenDecode() {
        return new TokenDecode();
    }

    @Bean
    public FeignInterceptor feignInterceptor() {
        return new FeignInterceptor();
    }
}
