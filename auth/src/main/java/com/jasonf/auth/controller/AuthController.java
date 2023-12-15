package com.jasonf.auth.controller;

import com.jasonf.auth.service.AuthService;
import com.jasonf.auth.util.AuthToken;
import com.jasonf.entity.Result;
import com.jasonf.entity.StatusCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("auth")
public class AuthController {
    @Value("${auth.clientId}")
    private String clientId;

    @Value("${auth.clientSecret}")
    private String clientSecret;

    @Value("${auth.cookieMaxAge}")
    private int cookieMaxAge;

    @Value("${auth.cookieDomain}")
    private String cookieDomain;

    @Resource
    private AuthService authService;

    @PostMapping("login")
    @ResponseBody
    public Result<String> login(String username, String password, HttpServletResponse response) {
        AuthToken authToken = authService.login(username, password, clientId, clientSecret);
        // 向 cookie 中写入 jti
        Cookie cookie = new Cookie("uid", authToken.getJti());
        cookie.setMaxAge(cookieMaxAge);
        cookie.setPath("/");
        cookie.setDomain(cookieDomain);
        cookie.setHttpOnly(false);
        response.addCookie(cookie);
        return new Result<>(true, StatusCode.OK, "登陆成功", authToken.getJti());
    }

    @GetMapping("toLogin")
    public String toLogin() {
        return "login";
    }
}
