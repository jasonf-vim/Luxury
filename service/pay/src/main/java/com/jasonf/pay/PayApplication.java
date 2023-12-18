package com.jasonf.pay;

import com.github.wxpay.sdk.WXConfig;
import com.github.wxpay.sdk.WXPay;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableEurekaClient
public class PayApplication {
    public static void main(String[] args) {
        SpringApplication.run(PayApplication.class, args);
    }

    @Bean
    public WXPay wxPay() {
        WXConfig wxConfig = new WXConfig();
        try {
            return new WXPay(wxConfig);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
