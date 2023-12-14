package com.jasonf.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.jasonf.search.pojo.SkuInfo;
import com.jasonf.search.service.SearchService;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl implements SearchService {
    @Resource
    private ElasticsearchTemplate elasticsearchTemplate;

    @Override
    public Map search(Map<String, String> params) {
        // 构建搜索条件
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if (params != null) {
            if (StringUtils.isNotEmpty(params.get("keywords"))) {
                MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("name", params.get("keywords"));
                boolQueryBuilder.must(matchQueryBuilder);
            }
            if (StringUtils.isNotEmpty(params.get("brand"))) {
                // 品牌类型为keyword, 采用term查询
                TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("brandName", params.get("brand"));
                boolQueryBuilder.filter(termQueryBuilder);    // term查询一般放到filter
            }
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (entry.getKey().startsWith("spec_")) {
                    String key = entry.getKey().substring(5);
                    String val = entry.getValue().replaceAll("%2B", "+");
                    TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("specMap." + key + ".keyword", val);
                    boolQueryBuilder.filter(termQueryBuilder);
                }
            }
            if (StringUtils.isNotEmpty(params.get("price"))) {
                String price = params.get("price");
                String[] split = price.split("-");
                RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("price").gte(split[0]);
                if (split.length == 2) {
                    rangeQueryBuilder.lte(split[1]);
                }
                boolQueryBuilder.filter(rangeQueryBuilder);
            }
        }
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        nativeSearchQueryBuilder.withQuery(boolQueryBuilder);

        // 分页
        String pageNum = params.getOrDefault("pageNum", "1");
        String pageSize = params.getOrDefault("pageSize", "30");
        PageRequest pageRequest = PageRequest.of(Integer.parseInt(pageNum) - 1, Integer.parseInt(pageSize));
        nativeSearchQueryBuilder.withPageable(pageRequest);

        // 排序
        if (StringUtils.isNotEmpty(params.get("sortField")) && StringUtils.isNotEmpty(params.get("sortRule"))) {
            if ("asc".equalsIgnoreCase(params.get("sortRule"))) {
                nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort(params.get("sortField")).order(SortOrder.ASC));
            } else {
                nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort(params.get("sortField")).order(SortOrder.DESC));
            }
        }

        // 高亮搜索
        HighlightBuilder.Field field = new HighlightBuilder.Field("name");
        field.preTags("<span style='color:red'>");
        field.postTags("</span>");
        nativeSearchQueryBuilder.withHighlightFields(field);

        // 根据品牌聚合
        String brandTerms = "brand";
        TermsAggregationBuilder brand = AggregationBuilders.terms(brandTerms).field("brandName");
        nativeSearchQueryBuilder.addAggregation(brand);
        // 根据规格聚合
        String specTerms = "spec";
        TermsAggregationBuilder spec = AggregationBuilders.terms(specTerms).field("spec.keyword");
        nativeSearchQueryBuilder.addAggregation(spec);

        // searchQuery          搜索条件
        // SkuInfo              实体类类型
        // SearchResultMapper   结果集映射
        AggregatedPage<SkuInfo> skuInfos = elasticsearchTemplate.queryForPage(nativeSearchQueryBuilder.build(), SkuInfo.class, new SearchResultMapper() {
            @Override
            public <T> AggregatedPage<T> mapResults(SearchResponse searchResponse, Class<T> aClass, Pageable pageable) {
                SearchHits hits = searchResponse.getHits();     // 大hits
                List<T> list = new ArrayList<>();
                for (SearchHit hit : hits.getHits()) {
                    String source = hit.getSourceAsString();
                    SkuInfo skuInfo = JSON.parseObject(source, SkuInfo.class);
                    HighlightField name = hit.getHighlightFields().get("name");
                    String renderName = "";
                    for (Text text : name.getFragments()) {
                        renderName += text.toString();
                    }
                    skuInfo.setName(renderName);
                    list.add((T) skuInfo);
                }
                return new AggregatedPageImpl<>(list, pageable, hits.getTotalHits(), searchResponse.getAggregations());
            }
        });

        // 封装结果
        Map resultMap = new HashMap();
        resultMap.put("total", skuInfos.getTotalElements());    // 结果总数
        resultMap.put("totalPages", skuInfos.getTotalPages());  // 总页数
        resultMap.put("content", skuInfos.getContent());    // 商品集
        // 品牌聚合结果
        StringTerms brandStringTerms = (StringTerms) skuInfos.getAggregation(brandTerms);
        List<String> brands = brandStringTerms.getBuckets().stream().map(bucket -> bucket.getKeyAsString()).collect(Collectors.toList());
        resultMap.put("brands", brands);
        // 规格聚合结果
        StringTerms specStringTerms = (StringTerms) skuInfos.getAggregation(specTerms);
        List<String> specs = specStringTerms.getBuckets().stream().map(bucket -> bucket.getKeyAsString()).collect(Collectors.toList());
        resultMap.put("specs", specs);
        return resultMap;
    }
}
