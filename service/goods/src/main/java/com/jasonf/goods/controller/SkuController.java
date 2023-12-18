package com.jasonf.goods.controller;

import com.github.pagehelper.Page;
import com.jasonf.entity.PageResult;
import com.jasonf.entity.Result;
import com.jasonf.entity.StatusCode;
import com.jasonf.goods.pojo.Sku;
import com.jasonf.goods.service.SkuService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/sku")
public class SkuController {
    @Resource
    private SkuService skuService;

    /**
     * 查询全部数据
     *
     * @return
     */
    @GetMapping
    public Result findAll() {
        List<Sku> skuList = skuService.findAll();
        return new Result(true, StatusCode.OK, "查询成功", skuList);
    }

    /***
     * 根据ID查询数据
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result findById(@PathVariable String id) {
        Sku sku = skuService.findById(id);
        return new Result(true, StatusCode.OK, "查询成功", sku);
    }

    @GetMapping("find/{id}")
    public Sku find(@PathVariable String id) {
        return skuService.findById(id);
    }


    /***
     * 新增数据
     * @param sku
     * @return
     */
    @PostMapping
    public Result add(@RequestBody Sku sku) {
        skuService.add(sku);
        return new Result(true, StatusCode.OK, "添加成功");
    }


    /***
     * 修改数据
     * @param sku
     * @param id
     * @return
     */
    @PutMapping(value = "/{id}")
    public Result update(@RequestBody Sku sku, @PathVariable String id) {
        sku.setId(id);
        skuService.update(sku);
        return new Result(true, StatusCode.OK, "修改成功");
    }


    /***
     * 根据ID删除品牌数据
     * @param id
     * @return
     */
    @DeleteMapping(value = "/{id}")
    public Result delete(@PathVariable String id) {
        skuService.delete(id);
        return new Result(true, StatusCode.OK, "删除成功");
    }

    /***
     * 多条件搜索品牌数据
     * @param searchMap
     * @return
     */
    @GetMapping(value = "/search")
    public Result findList(@RequestParam Map searchMap) {
        List<Sku> list = skuService.findList(searchMap);
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
        Page<Sku> pageList = skuService.findPage(searchMap, page, size);
        PageResult pageResult = new PageResult(pageList.getTotal(), pageList.getResult());
        return new Result(true, StatusCode.OK, "查询成功", pageResult);
    }

    @GetMapping("spu/{spuId}")
    public List<Sku> findSkuListBySpuId(@PathVariable("spuId") String spuId) {
        Map<String, Object> condition = new HashMap<>();
        condition.put("status", "1");   // 正常状态
        if (!"all".equals(spuId)) {
            condition.put("spuId", spuId);
        }   // 非初始入库
        return skuService.findList(condition);
    }

    @PutMapping("/decr/count")
    public Result decrCount(@RequestParam("username") String username) {
        skuService.decrCount(username);
        return new Result(true, StatusCode.OK, "库存修改成功");
    }

    @PutMapping("/rollback")
    public Result rollback(@RequestParam("skuId") String skuId, @RequestParam("num") Integer num) {
        skuService.rollback(skuId, num);
        return new Result(true, StatusCode.OK, "库存回滚成功");
    }
}
