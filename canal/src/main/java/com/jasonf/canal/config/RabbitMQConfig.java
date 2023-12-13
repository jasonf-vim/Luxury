package com.jasonf.canal.config;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    public static final String AD_UPDATE_QUEUE = "ad_update_queue";

    public static final String GOODS_UP_EXCHANGE = "goods_up_exchange";

    public static final String SEARCH_ADD_QUEUE = "search_add_queue";

    public static final String GOODS_DOWN_EXCHANGE = "goods_down_exchange";

    public static final String SEARCH_DELETE_QUEUE = "search_delete_queue";

    @Bean
    public Queue queue() {
        return new Queue(AD_UPDATE_QUEUE, true);
    }

    @Bean(SEARCH_ADD_QUEUE)
    public Queue goodsUpQueue() {
        return new Queue(SEARCH_ADD_QUEUE, true);
    }

    @Bean(GOODS_UP_EXCHANGE)
    public Exchange goodsUpExchange() {
        return ExchangeBuilder.fanoutExchange(GOODS_UP_EXCHANGE).durable(true).build();
    }

    @Bean
    public Binding binding(@Qualifier(SEARCH_ADD_QUEUE) Queue queue,
                           @Qualifier(GOODS_UP_EXCHANGE) Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with("").noargs();
    }

    @Bean(SEARCH_DELETE_QUEUE)
    public Queue goodsDownQueue() {
        return new Queue(SEARCH_DELETE_QUEUE, true);
    }

    @Bean(GOODS_DOWN_EXCHANGE)
    public Exchange goodsDownExchange() {
        return ExchangeBuilder.fanoutExchange(GOODS_DOWN_EXCHANGE).durable(true).build();
    }

    @Bean
    public Binding bind(@Qualifier(SEARCH_DELETE_QUEUE) Queue queue,
                        @Qualifier(GOODS_DOWN_EXCHANGE) Exchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with("").noargs();
    }
}
