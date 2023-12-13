package com.jasonf.search.controller;

import com.jasonf.entity.Result;
import com.jasonf.entity.StatusCode;
import com.jasonf.search.service.ESManagerService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("es-manager")
public class ESManagerController {
    @Resource
    private ESManagerService esManagerService;

    @GetMapping("create")
    public Result create() {
        esManagerService.createIndexAndMapping();
        return new Result(true, StatusCode.OK, "成功创建索引和映射");
    }

    @GetMapping("import-data")
    public Result importData() {
        esManagerService.importData();
        return new Result(true, StatusCode.OK, "导入成功");
    }
}
