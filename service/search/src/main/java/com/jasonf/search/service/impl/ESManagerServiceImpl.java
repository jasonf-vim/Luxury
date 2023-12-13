package com.jasonf.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.jasonf.goods.feign.SkuFeign;
import com.jasonf.goods.pojo.Sku;
import com.jasonf.search.dao.ESManagerMapper;
import com.jasonf.search.pojo.SkuInfo;
import com.jasonf.search.service.ESManagerService;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ESManagerServiceImpl implements ESManagerService {
    @Resource
    private ElasticsearchTemplate elasticsearchTemplate;

    @Resource
    private SkuFeign skuFeign;

    @Resource
    private ESManagerMapper esManagerMapper;

    @Override
    public void createIndexAndMapping() {
        elasticsearchTemplate.createIndex(SkuInfo.class);
        elasticsearchTemplate.putMapping(SkuInfo.class);
    }

    @Override
    public void importData() {
        // 查询现存所有sku
        List<Sku> skus = skuFeign.findSkuListBySpuId("all");
        System.out.println("sku现存总数量：" + skus.size());
        // 导入es
        List<SkuInfo> skuInfos = new ArrayList<>();
        for (Sku sku : skus) {
            SkuInfo skuInfo = JSON.parseObject(JSON.toJSONString(sku), SkuInfo.class);
            skuInfo.setSpecMap(JSON.parseObject(sku.getSpec(), Map.class));
            skuInfos.add(skuInfo);
        }
        esManagerMapper.saveAll(skuInfos);
    }

    @Override
    public void importDataBySpuId(String spuId) {
        List<Sku> skus = skuFeign.findSkuListBySpuId(spuId);
        List<SkuInfo> skuInfos = new ArrayList<>();     // 导入es
        for (Sku sku : skus) {
            SkuInfo skuInfo = JSON.parseObject(JSON.toJSONString(sku), SkuInfo.class);
            skuInfo.setSpecMap(JSON.parseObject(sku.getSpec(), Map.class));
            skuInfos.add(skuInfo);
        }
        esManagerMapper.saveAll(skuInfos);
    }

    @Override
    public void removeDataBySpuId(String spuId) {
        List<Sku> skus = skuFeign.findSkuListBySpuId(spuId);
        if (skus == null || skus.isEmpty()) {
            return;
        }
        for (Sku sku : skus) {
            esManagerMapper.deleteById(Long.parseLong(sku.getId()));
        }
    }
}
