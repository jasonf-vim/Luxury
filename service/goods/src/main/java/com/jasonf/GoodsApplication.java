package com.jasonf;

import com.jasonf.utils.IdWorker;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication
@EnableEurekaClient
@MapperScan(basePackages = {"com.jasonf.goods.dao"})
public class GoodsApplication {
    @Value("${workerId}")
    Long workerId;

    @Value("${dataCenterId}")
    Long dataCenterId;

    public static void main(String[] args) {
        SpringApplication.run(GoodsApplication.class);
    }

    @Bean
    public IdWorker idWorker() {
        return new IdWorker(workerId, dataCenterId);
    }
}
