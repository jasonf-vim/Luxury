package com.jasonf.user.controller;

import com.github.pagehelper.Page;
import com.jasonf.entity.PageResult;
import com.jasonf.entity.Result;
import com.jasonf.entity.StatusCode;
import com.jasonf.user.pojo.Areas;
import com.jasonf.user.service.AreasService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/areas")
public class AreasController {
    @Resource
    private AreasService areasService;

    /**
     * 查询全部数据
     *
     * @return
     */
    @GetMapping
    public Result findAll() {
        List<Areas> areasList = areasService.findAll();
        return new Result(true, StatusCode.OK, "查询成功", areasList);
    }

    /***
     * 根据ID查询数据
     * @param areaid
     * @return
     */
    @GetMapping("/{areaid}")
    public Result findById(@PathVariable String areaid) {
        Areas areas = areasService.findById(areaid);
        return new Result(true, StatusCode.OK, "查询成功", areas);
    }


    /***
     * 新增数据
     * @param areas
     * @return
     */
    @PostMapping
    public Result add(@RequestBody Areas areas) {
        areasService.add(areas);
        return new Result(true, StatusCode.OK, "添加成功");
    }


    /***
     * 修改数据
     * @param areas
     * @param areaid
     * @return
     */
    @PutMapping(value = "/{areaid}")
    public Result update(@RequestBody Areas areas, @PathVariable String areaid) {
        areas.setAreaId(areaid);
        areasService.update(areas);
        return new Result(true, StatusCode.OK, "修改成功");
    }


    /***
     * 根据ID删除品牌数据
     * @param areaid
     * @return
     */
    @DeleteMapping(value = "/{areaid}")
    public Result delete(@PathVariable String areaid) {
        areasService.delete(areaid);
        return new Result(true, StatusCode.OK, "删除成功");
    }

    /***
     * 多条件搜索品牌数据
     * @param searchMap
     * @return
     */
    @GetMapping(value = "/search")
    public Result findList(@RequestParam Map searchMap) {
        List<Areas> list = areasService.findList(searchMap);
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
        Page<Areas> pageList = areasService.findPage(searchMap, page, size);
        PageResult pageResult = new PageResult(pageList.getTotal(), pageList.getResult());
        return new Result(true, StatusCode.OK, "查询成功", pageResult);
    }
}
