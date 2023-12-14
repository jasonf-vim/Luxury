package com.jasonf.search.controller;

import com.alibaba.fastjson.JSON;
import com.jasonf.entity.Page;
import com.jasonf.search.pojo.SkuInfo;
import com.jasonf.search.service.SearchService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.*;

@Controller
@RequestMapping("search")
public class SearchController {
    @Resource
    private SearchService searchService;

    @GetMapping
    @ResponseBody
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

    @GetMapping("list")
    public String list(@RequestParam Map<String, String> params, Model model) {
        StringBuilder url = new StringBuilder("/search/list");
        if (params != null && params.size() > 0) {
            url.append("?");
            for (Map.Entry<String, String> param : params.entrySet()) {
                String key = param.getKey();
                if (!key.equals("sortField") && !key.equals("sortRule") && !key.equals("pageNum") && !key.equals("pageSize")) {
                    url.append(key).append("=").append(param.getValue()).append("&");
                }
            }
            String urlStr = url.substring(0, url.length() - 1);
            model.addAttribute("url", urlStr);
        } else {
            model.addAttribute("url", url.toString());
        }
        model.addAttribute("searchMap", params);
        handleEncode(params);
        Map resultMap = searchService.search(params);
        resultMap.put("specs", specFormat((List<String>) resultMap.get("specs")));
        model.addAttribute("result", resultMap);

        Page<SkuInfo> page = new Page<>((Long) resultMap.get("total"),
                Integer.parseInt(String.valueOf(resultMap.get("pageNum"))),
                Integer.parseInt(String.valueOf(resultMap.get("pageSize"))));
        model.addAttribute("page", page);
        return "search";
    }

    /**
     * 规格分类
     */
    private Map<String, Set<String>> specFormat(List<String> specs) {
        Map<String, Set<String>> specMap = new HashMap<>();
        for (String spec : specs) {
            Map<String, String> map = JSON.parseObject(spec, Map.class);
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String key = entry.getKey();
                String val = entry.getValue();
                if (!specMap.containsKey(key)) {
                    specMap.put(key, new HashSet<>());
                }
                specMap.get(key).add(val);
            }
        }
        return specMap;
    }
}
