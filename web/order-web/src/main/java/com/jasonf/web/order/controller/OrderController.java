package com.jasonf.web.order.controller;

import com.jasonf.entity.Result;
import com.jasonf.order.feign.CartFeign;
import com.jasonf.order.feign.OrderFeign;
import com.jasonf.order.pojo.Order;
import com.jasonf.order.pojo.OrderItem;
import com.jasonf.user.feign.AddressFeign;
import com.jasonf.user.pojo.Address;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("worder")
public class OrderController {
    @Resource
    private AddressFeign addressFeign;

    @Resource
    private CartFeign cartFeign;

    @Resource
    private OrderFeign orderFeign;

    @GetMapping("ready/order")
    public String readyOrder(Model model) {
        // 收件人信息
        List<Address> addressList = addressFeign.list();
        model.addAttribute("address", addressList);
        // 默认地址
        for (Address address : addressList) {
            if ("1".equals(address.getIsDefault())) {
                model.addAttribute("deAddr", address);
                break;
            }
        }
        // 购物车信息
        Map map = cartFeign.list().getData();
        List<OrderItem> orderItems = (List<OrderItem>) map.get("orderItems");
        model.addAttribute("carts", orderItems);    // 商品详细信息
        Integer totalNum = (Integer) map.get("totalNum");
        model.addAttribute("totalNum", totalNum);   // 总件数
        Integer totalMoney = (Integer) map.get("totalMoney");
        model.addAttribute("totalMoney", totalMoney);   // 总金额
        return "order";
    }

    @PostMapping("add")
    @ResponseBody
    public Result add(@RequestBody Order order) {
        return orderFeign.add(order);
    }

    @GetMapping("/toPayPage")
    public String toPayPage(String orderId, Model model) {
        model.addAttribute("orderId", orderId);
        Order order = (Order) orderFeign.findById(orderId).getData();
        model.addAttribute("payMoney", order.getPayMoney());
        return "pay";
    }
}
