package com.jasonf.order.controller;

import com.github.pagehelper.Page;
import com.jasonf.entity.PageResult;
import com.jasonf.entity.Result;
import com.jasonf.entity.StatusCode;
import com.jasonf.order.config.TokenDecode;
import com.jasonf.order.pojo.Order;
import com.jasonf.order.service.OrderService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/order")
public class OrderController {
    @Resource
    private OrderService orderService;

    @Resource
    private TokenDecode tokenDecode;

    /**
     * 查询全部数据
     *
     * @return
     */
    @GetMapping
    public Result findAll() {
        List<Order> orderList = orderService.findAll();
        return new Result(true, StatusCode.OK, "查询成功", orderList);
    }

    /***
     * 根据ID查询数据
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result findById(@PathVariable String id) {
        Order order = orderService.findById(id);
        return new Result(true, StatusCode.OK, "查询成功", order);
    }


    /***
     * 新增数据
     * @param order
     * @return
     */
    @PostMapping
    public Result add(@RequestBody Order order) {
        String username = tokenDecode.getUserInfo().get("username");
        order.setUsername(username);
        String orderId = orderService.add(order);
        return new Result(true, StatusCode.OK, "添加成功", orderId);
    }


    /***
     * 修改数据
     * @param order
     * @param id
     * @return
     */
    @PutMapping(value = "/{id}")
    public Result update(@RequestBody Order order, @PathVariable String id) {
        order.setId(id);
        orderService.update(order);
        return new Result(true, StatusCode.OK, "修改成功");
    }


    /***
     * 根据ID删除品牌数据
     * @param id
     * @return
     */
    @DeleteMapping(value = "/{id}")
    public Result delete(@PathVariable String id) {
        orderService.delete(id);
        return new Result(true, StatusCode.OK, "删除成功");
    }

    /***
     * 多条件搜索品牌数据
     * @param searchMap
     * @return
     */
    @GetMapping(value = "/search")
    public Result findList(@RequestParam Map searchMap) {
        List<Order> list = orderService.findList(searchMap);
        return new Result(true, StatusCode.OK, "查询成功", list);
    }


    /***
     * 分页搜索实现
     * @param searchMap
     * @param page
     * @param size
     * @return
     */
    @GetMapping(value = "/search/{page}/{size}")
    public Result findPage(@RequestParam Map searchMap, @PathVariable int page, @PathVariable int size) {
        Page<Order> pageList = orderService.findPage(searchMap, page, size);
        PageResult pageResult = new PageResult(pageList.getTotal(), pageList.getResult());
        return new Result(true, StatusCode.OK, "查询成功", pageResult);
    }

    @PostMapping("batchSend")
    public Result batchSend(@RequestBody List<Order> orders) {
        orderService.batchSend(orders);
        return new Result(true, StatusCode.OK, "批量发货成功");
    }

    // 确认收货
    @PutMapping("take/{orderId}/operator/{operator}")
    public Result take(@PathVariable("orderId") String orderId, @PathVariable("operator") String operator) {
        orderService.take(orderId, operator);
        return new Result(true, StatusCode.OK, "已确认收货");
    }
}
