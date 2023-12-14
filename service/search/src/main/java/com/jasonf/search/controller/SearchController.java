package com.jasonf.search.controller;

import com.jasonf.search.service.SearchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@RequestMapping("search")
public class SearchController {
    @Resource
    private SearchService searchService;

    @GetMapping
    public Map search(@RequestParam Map<String, String> params) {
        handleEncode(params);
        return searchService.search(params);
    }

    private void handleEncode(Map<String, String> params) {
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (entry.getKey().startsWith("spec_")) {
                String newStr = entry.getValue().replaceAll("\\+", "%2B");
                params.put(entry.getKey(), newStr);
            }
        }
    }
}
