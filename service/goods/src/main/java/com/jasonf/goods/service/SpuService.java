package com.jasonf.goods.service;

import com.github.pagehelper.Page;
import com.jasonf.goods.pojo.Goods;
import com.jasonf.goods.pojo.Spu;

import java.util.List;
import java.util.Map;

public interface SpuService {
    /**
     * 查询所有
     *
     * @return
     */
    List<Spu> findAll();

    /**
     * 根据ID查询
     *
     * @param spuId
     * @return
     */
    Goods findById(String spuId);

    /**
     * 新增
     *
     * @param goods
     */
    void add(Goods goods);

    /**
     * 修改
     *
     * @param goods
     */
    void update(Goods goods);

    /**
     * 删除
     *
     * @param id
     */
    void delete(String id);

    /**
     * 多条件搜索
     *
     * @param searchMap
     * @return
     */
    List<Spu> findList(Map<String, Object> searchMap);

    /**
     * 分页查询
     *
     * @param page
     * @param size
     * @return
     */
    Page<Spu> findPage(int page, int size);

    /**
     * 多条件分页查询
     *
     * @param searchMap
     * @param page
     * @param size
     * @return
     */
    Page<Spu> findPage(Map<String, Object> searchMap, int page, int size);

    /**
     * 后台商品审核
     *
     * @param id
     */
    void audit(String id);

    /**
     * 商品下架
     *
     * @param id
     */
    void pull(String id);

    /**
     * 商品上架
     *
     * @param id
     */
    void put(String id);

    /**
     * 还原商品
     *
     * @param id
     */
    void restore(String id);

    /**
     * 物理删除商品
     *
     * @param id
     */
    void realDelete(String id);

    /**
     * 页面服务查询spu信息
     * @param id
     * @return
     */
    Spu findSpuById(String id);
}
