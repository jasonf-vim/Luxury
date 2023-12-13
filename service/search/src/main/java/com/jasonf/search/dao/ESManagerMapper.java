package com.jasonf.search.dao;

import com.jasonf.search.pojo.SkuInfo;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

public interface ESManagerMapper extends ElasticsearchRepository<SkuInfo, Long> {
}
