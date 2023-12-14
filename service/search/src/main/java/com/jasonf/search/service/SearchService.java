package com.jasonf.search.service;

import java.util.Map;

public interface SearchService {
    /**
     * 商品检索
     *
     * @param param 搜索参数
     * @return
     */
    Map search(Map<String, String> param);
}
