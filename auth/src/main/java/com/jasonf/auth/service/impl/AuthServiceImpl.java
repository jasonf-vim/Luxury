package com.jasonf.auth.service.impl;

import com.jasonf.auth.service.AuthService;
import com.jasonf.auth.util.AuthToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class AuthServiceImpl implements AuthService {
    @Value("${auth.ttl}")
    private long ttl;

    @Resource
    private RestTemplate restTemplate;

    @Resource
    private LoadBalancerClient loadBalancerClient;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public AuthToken login(String username, String password, String clientId, String clientSecret) {
        // 向 oauth2 申请令牌
        ServiceInstance authService = loadBalancerClient.choose("AUTH");
        String url = authService.getUri() + "/oauth/token";     // 负载均衡

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();     // 请求体
        body.add("grant_type", "password");
        body.add("username", username);
        body.add("password", password);

        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();    // 请求头
        headers.add("Authorization", buildAuthHeader(clientId, clientSecret));

        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(body, headers);

        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {
                if (response.getRawStatusCode() != 400 && response.getRawStatusCode() != 401) {
                    super.handleError(response);
                }
            }
        });
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, Map.class);

        // 构建返回结果
        Map<String, String> respMap = response.getBody();
        if (respMap == null || !respMap.containsKey("access_token") || !respMap.containsKey("refresh_token") || !respMap.containsKey("jti")) {
            throw new RuntimeException("申请令牌失败");
        }
        AuthToken authToken = new AuthToken();
        authToken.setAccessToken(respMap.get("access_token"));
        authToken.setRefreshToken(respMap.get("refresh_token"));
        authToken.setJti(respMap.get("jti"));

        // 在 redis 中存入 jti:jwt
        stringRedisTemplate.opsForValue().set(authToken.getJti(), authToken.getAccessToken(), ttl, TimeUnit.SECONDS);

        return authToken;
    }

    private String buildAuthHeader(String clientId, String clientSecret) {
        // 格式：Basic base64(id:secret)
        String src = clientId + ":" + clientSecret;
        String encode = Base64Utils.encodeToString(src.getBytes());
        return "Basic " + encode;
    }
}
