package com.jasonf.gateway.filters;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class IpFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        final ServerHttpRequest request = exchange.getRequest();
        final ServerHttpResponse response = exchange.getResponse();
        final String hostString = request.getRemoteAddress().getHostString();
        if ("192.168.3.54".equals(hostString)) {
            response.setStatusCode(HttpStatus.FORBIDDEN);   // 禁止访问
            return response.setComplete();
        }
        return chain.filter(exchange);  // 放行
    }

    @Override
    public int getOrder() {
        return 1;   // 过滤优先级，数值越小越优先
    }
}
