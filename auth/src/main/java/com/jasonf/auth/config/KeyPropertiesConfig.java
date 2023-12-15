package com.jasonf.auth.config;

import org.springframework.cloud.bootstrap.encrypt.KeyProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeyPropertiesConfig {
    @Bean
    public KeyProperties keyProperties(){
        return new KeyProperties();
    }
}
