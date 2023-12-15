package com.jasonf.web.gateway.filter;

import com.jasonf.web.gateway.utils.UrlUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;

@Component
public class AuthFilter implements GlobalFilter, Ordered {
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        // 登录业务：放行
        if (!UrlUtil.requireAuthorize(request.getURI().getPath())) {
            return chain.filter(exchange);
        }
        // 没有响应的cookie，拒绝
        MultiValueMap<String, HttpCookie> cookies = request.getCookies();
        HttpCookie jti = cookies.getFirst("uid");
        if (jti == null) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
        // redis缓存中查询不到相应的jti，拒绝
        String jwt = stringRedisTemplate.opsForValue().get(jti.getValue());
        if (StringUtils.isEmpty(jwt)) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
        // 拼接 jwt 到请求头中
        request.mutate().header("Authorization", "Bearer " + jwt);  // 由微服务判断是否合法
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
