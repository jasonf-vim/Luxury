package com.jasonf.search.service;

public interface ESManagerService {
    /**
     * 创建索引库、映射
     */
    void createIndexAndMapping();

    /**
     * 导入现存所有的sku
     */
    void importData();

    /**
     * 根据spuId导入sku
     *
     * @param spuId
     */
    void importDataBySpuId(String spuId);

    /**
     * 根据spuId移除sku
     * @param spuId
     */
    void removeDataBySpuId(String spuId);
}
