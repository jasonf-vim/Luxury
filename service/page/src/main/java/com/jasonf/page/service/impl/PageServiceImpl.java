package com.jasonf.page.service.impl;

import com.alibaba.fastjson.JSON;
import com.jasonf.goods.feign.CategoryFeign;
import com.jasonf.goods.feign.SkuFeign;
import com.jasonf.goods.feign.SpuFeign;
import com.jasonf.goods.pojo.Category;
import com.jasonf.goods.pojo.Sku;
import com.jasonf.goods.pojo.Spu;
import com.jasonf.page.service.PageService;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PageServiceImpl implements PageService {
    @Resource
    private TemplateEngine templateEngine;

    @Value("${pagePath}")
    private String path;

    @Resource
    private SpuFeign spuFeign;

    @Resource
    private SkuFeign skuFeign;

    @Resource
    private CategoryFeign categoryFeign;

    // 页面静态化
    @Override
    public void generatePage(String spuId) {
        // 模版数据
        Context context = new Context();
        context.setVariables(getData(spuId));

        // 文件
        File dir = new File(path);
        if (!dir.exists()) {
            dir.mkdirs();   // 建立多级目录
        }

        try (Writer writer = new FileWriter(path + "/" + spuId + ".html");) {
            templateEngine.process("item", context, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Map<String, Object> getData(String spuId) {
        Map<String, Object> resultMap = new HashMap<>();
        // spu
        Spu spu = spuFeign.findSpuById(spuId);
        resultMap.put("spu", spu);

        // skuList
        List<Sku> skuList = skuFeign.findSkuListBySpuId(spuId);
        resultMap.put("skuList", skuList);

        // category
        Category category1 = categoryFeign.findCategoryById(spu.getCategory1Id());
        Category category2 = categoryFeign.findCategoryById(spu.getCategory2Id());
        Category category3 = categoryFeign.findCategoryById(spu.getCategory3Id());
        resultMap.put("category1", category1);
        resultMap.put("category2", category2);
        resultMap.put("category3", category3);

        // imageList
        String images = spu.getImages();
        if (StringUtils.isNotEmpty(images)) {
            String[] imageList = images.split(",");
            resultMap.put("imageList", imageList);
        }

        // specificationList
        String specItems = spu.getSpecItems();
        if (StringUtils.isNotEmpty(specItems)) {
            Map specMap = JSON.parseObject(specItems, Map.class);
            resultMap.put("specificationList", specMap);
        }
        return resultMap;
    }
}
