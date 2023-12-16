package com.jasonf.web.order.controller;

import com.jasonf.entity.Result;
import com.jasonf.order.feign.CartFeign;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.Map;

@Controller
@RequestMapping("wcart")
public class CartController {
    @Resource
    private CartFeign cartFeign;

    @GetMapping("list")
    public String list(Model model) {
        Map map = cartFeign.list().getData();
        model.addAttribute("items", map);
        return "cart";
    }

    @GetMapping("add")
    @ResponseBody
    public Result add(String skuId, Integer num) {
        cartFeign.addCart(skuId, num);
        return cartFeign.list();    // 用于局部刷新数据
    }
}
