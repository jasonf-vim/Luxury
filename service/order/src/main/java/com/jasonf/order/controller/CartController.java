package com.jasonf.order.controller;

import com.jasonf.entity.Result;
import com.jasonf.entity.StatusCode;
import com.jasonf.order.config.TokenDecode;
import com.jasonf.order.service.CartService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("/cart")
public class CartController {
    @Resource
    private CartService cartService;

    @Resource
    private TokenDecode tokenDecode;

    @GetMapping("/addCart")
    public Result addCart(@RequestParam("skuId") String skuId,
                          @RequestParam("num") Integer num) {
        String username = tokenDecode.getUserInfo().get("username");
        cartService.addCart(skuId, num, username);
        return new Result(true, StatusCode.OK, "添加购物车成功");
    }

    @GetMapping("/list")
    public Result<Map> list() {
        String username = tokenDecode.getUserInfo().get("username");
        Map map = cartService.list(username);
        return new Result<>(true, StatusCode.OK, "查询成功", map);
    }
}
