package com.jasonf.gateway.filters;

import com.jasonf.gateway.utils.JwtUtil;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

public class AuthorizeFilter implements GlobalFilter, Ordered {
    private static final String AUTHORIZE_TOKEN = "token";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        // 登陆放行
        String path = request.getURI().getPath();
        if (path.contains("/admin/login")) {
            return chain.filter(exchange);
        }

        // jwt信息为空
        HttpHeaders headers = request.getHeaders();
        List<String> tokens = headers.get(AUTHORIZE_TOKEN);
        if (tokens == null || tokens.isEmpty()) {   // 头部不附带token信息
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
        String token = tokens.get(0);

        // 校验jwt信息
        try {
            JwtUtil.parseJWT(token);
        } catch (Exception ex) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
