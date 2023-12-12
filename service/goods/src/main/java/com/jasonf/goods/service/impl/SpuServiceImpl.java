package com.jasonf.goods.service.impl;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.jasonf.goods.dao.*;
import com.jasonf.goods.pojo.*;
import com.jasonf.goods.service.SpuService;
import com.jasonf.utils.IdWorker;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class SpuServiceImpl implements SpuService {
    @Resource
    private SpuMapper spuMapper;

    @Resource
    private IdWorker idWorker;

    @Resource
    private SkuMapper skuMapper;

    @Resource
    private CategoryMapper categoryMapper;

    @Resource
    private BrandMapper brandMapper;

    @Resource
    private CategoryBrandMapper categoryBrandMapper;

    /**
     * 查询全部列表
     *
     * @return
     */
    @Override
    public List<Spu> findAll() {
        return spuMapper.selectAll();
    }

    /**
     * 根据ID查询
     *
     * @param spuId
     * @return
     */
    @Override
    public Goods findById(String spuId) {
        // 查询spu信息
        Spu spu = spuMapper.selectByPrimaryKey(spuId);
        // 查询skuList信息
        Example example = new Example(Sku.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("spuId", spuId);
        List<Sku> skus = skuMapper.selectByExample(example);
        // 封装goods
        return new Goods(spu, skus);
    }


    /**
     * 增加
     *
     * @param goods
     */
    @Override
    public void add(Goods goods) {
        // 插入spu
        Spu spu = goods.getSpu();
        spu.setId(Long.toString(idWorker.nextId()));
        spu.setIsMarketable("0");   // 未上架
        spu.setIsEnableSpec("1");   // 启用规格
        spu.setIsDelete("0");   // 未删除
        spu.setStatus("0");     // 待审核
        spuMapper.insertSelective(spu);
        // 插入skuList
        saveSkuList(goods);
    }

    private void saveSkuList(Goods goods) {
        Spu spu = goods.getSpu();
        String spuId = spu.getId();
        Integer category3Id = spu.getCategory3Id();
        Category category = categoryMapper.selectByPrimaryKey(category3Id);
        Integer brandId = spu.getBrandId();
        Brand brand = brandMapper.selectByPrimaryKey(brandId);
        for (Sku sku : goods.getSkuList()) {
            sku.setId(Long.toString(idWorker.nextId()));
            // skuName = spuName + specValue
            String specName = goods.getSpu().getName();     // spuName
            String spec = sku.getSpec();
            if (StringUtils.isEmpty(spec) || StringUtils.isBlank(spec)) {
                spec = "{}";
            }   // 防止规格值不合法
            Map specMap = JSON.parseObject(spec, Map.class);
            if (specMap != null && specMap.size() > 0) {
                for (Object value : specMap.values()) {
                    specName += " " + value;
                }
            }   // 防止map为空
            sku.setName(specName);
            sku.setCreateTime(new Date());
            sku.setUpdateTime(new Date());
            sku.setSpuId(spuId);
            sku.setCategoryId(category.getId());
            sku.setCategoryName(category.getName());
            sku.setBrandName(brand.getName());
            sku.setSaleNum(0);  // 已售出数量
            sku.setCommentNum(0);   // 评论数
            sku.setStatus("0");     // 状态正常

            skuMapper.insertSelective(sku);

            // 维护品牌分类关联
            CategoryBrand categoryBrand = new CategoryBrand();
            categoryBrand.setCategoryId(category3Id);
            categoryBrand.setBrandId(brandId);
            int rows = categoryBrandMapper.selectCount(categoryBrand);
            if (rows <= 0) {
                categoryBrandMapper.insert(categoryBrand);
            }
        }
    }

    /**
     * 修改
     *
     * @param goods
     */
    @Override
    public void update(Goods goods) {
        // 修改spu
        Spu spu = goods.getSpu();
        spuMapper.updateByPrimaryKeySelective(spu);
        // 删除原本关联的sku
        Example example = new Example(Sku.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("spuId", spu.getId());
        skuMapper.deleteByExample(example);
        // 插入新的skuList
        saveSkuList(goods);
    }

    /**
     * 删除
     *
     * @param id
     */
    @Override
    public void delete(String id) {
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if (spu == null) {
            throw new RuntimeException("当前商品不存在");
        }
        if ("1".equals(spu.getIsMarketable())) {
            throw new RuntimeException("当前商品正上架");
        }
        spu.setIsDelete("1");   // 逻辑删除
        spuMapper.updateByPrimaryKeySelective(spu);
    }


    /**
     * 条件查询
     *
     * @param searchMap
     * @return
     */
    @Override
    public List<Spu> findList(Map<String, Object> searchMap) {
        Example example = createExample(searchMap);
        return spuMapper.selectByExample(example);
    }

    /**
     * 分页查询
     *
     * @param page
     * @param size
     * @return
     */
    @Override
    public Page<Spu> findPage(int page, int size) {
        PageHelper.startPage(page, size);
        return (Page<Spu>) spuMapper.selectAll();
    }

    /**
     * 条件+分页查询
     *
     * @param searchMap 查询条件
     * @param page      页码
     * @param size      页大小
     * @return 分页结果
     */
    @Override
    public Page<Spu> findPage(Map<String, Object> searchMap, int page, int size) {
        PageHelper.startPage(page, size);
        Example example = createExample(searchMap);
        return (Page<Spu>) spuMapper.selectByExample(example);
    }

    @Override
    public void audit(String id) {
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if (spu == null) {
            throw new RuntimeException("当前商品不存在");
        }
        if ("1".equals(spu.getIsDelete())) {
            throw new RuntimeException("当前商品已删除");
        }
        spu.setStatus("1"); // 审核通过
        spu.setIsMarketable("1");   // 上架商品
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    @Override
    public void pull(String id) {
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if (spu == null) {
            throw new RuntimeException("当前商品不存在");
        }
        if ("1".equals(spu.getIsDelete())) {
            throw new RuntimeException("当前商品已删除");
        }
        spu.setIsMarketable("0");   // 下架商品
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    @Override
    public void put(String id) {
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if (spu == null) {
            throw new RuntimeException("当前商品不存在");
        }
        if (!"1".equals(spu.getStatus())) {
            throw new RuntimeException("当前商品未通过审核");
        }
        spu.setIsMarketable("1");   // 上架商品
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    @Override
    public void restore(String id) {
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if (spu == null) {
            throw new RuntimeException("当前商品不存在");
        }
        spu.setIsDelete("0");
        spu.setStatus("0"); // 恢复后需重新审核
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    @Override
    public void realDelete(String id) {
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if (spu == null) {
            throw new RuntimeException("当前商品不存在");
        }
        // 必须确认为已经逻辑删除
        if (!"1".equals(spu.getIsDelete())) {
            throw new RuntimeException("当前商品暂未逻辑删除");
        }
        spuMapper.deleteByPrimaryKey(id);   // 物理删除
    }

    /**
     * 构建查询对象
     *
     * @param searchMap
     * @return
     */
    private Example createExample(Map<String, Object> searchMap) {
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        if (searchMap != null) {
            // 主键
            if (searchMap.get("id") != null && !"".equals(searchMap.get("id"))) {
                criteria.andEqualTo("id", searchMap.get("id"));
            }
            // 货号
            if (searchMap.get("sn") != null && !"".equals(searchMap.get("sn"))) {
                criteria.andEqualTo("sn", searchMap.get("sn"));
            }
            // SPU名
            if (searchMap.get("name") != null && !"".equals(searchMap.get("name"))) {
                criteria.andLike("name", "%" + searchMap.get("name") + "%");
            }
            // 副标题
            if (searchMap.get("caption") != null && !"".equals(searchMap.get("caption"))) {
                criteria.andLike("caption", "%" + searchMap.get("caption") + "%");
            }
            // 图片
            if (searchMap.get("image") != null && !"".equals(searchMap.get("image"))) {
                criteria.andLike("image", "%" + searchMap.get("image") + "%");
            }
            // 图片列表
            if (searchMap.get("images") != null && !"".equals(searchMap.get("images"))) {
                criteria.andLike("images", "%" + searchMap.get("images") + "%");
            }
            // 售后服务
            if (searchMap.get("saleService") != null && !"".equals(searchMap.get("saleService"))) {
                criteria.andLike("saleService", "%" + searchMap.get("saleService") + "%");
            }
            // 介绍
            if (searchMap.get("introduction") != null && !"".equals(searchMap.get("introduction"))) {
                criteria.andLike("introduction", "%" + searchMap.get("introduction") + "%");
            }
            // 规格列表
            if (searchMap.get("specItems") != null && !"".equals(searchMap.get("specItems"))) {
                criteria.andLike("specItems", "%" + searchMap.get("specItems") + "%");
            }
            // 参数列表
            if (searchMap.get("paraItems") != null && !"".equals(searchMap.get("paraItems"))) {
                criteria.andLike("paraItems", "%" + searchMap.get("paraItems") + "%");
            }
            // 是否上架
            if (searchMap.get("isMarketable") != null && !"".equals(searchMap.get("isMarketable"))) {
                criteria.andEqualTo("isMarketable", searchMap.get("isMarketable"));
            }
            // 是否启用规格
            if (searchMap.get("isEnableSpec") != null && !"".equals(searchMap.get("isEnableSpec"))) {
                criteria.andEqualTo("isEnableSpec", searchMap.get("isEnableSpec"));
            }
            // 是否删除
            if (searchMap.get("isDelete") != null && !"".equals(searchMap.get("isDelete"))) {
                criteria.andEqualTo("isDelete", searchMap.get("isDelete"));
            }
            // 审核状态
            if (searchMap.get("status") != null && !"".equals(searchMap.get("status"))) {
                criteria.andEqualTo("status", searchMap.get("status"));
            }

            // 品牌ID
            if (searchMap.get("brandId") != null) {
                criteria.andEqualTo("brandId", searchMap.get("brandId"));
            }
            // 一级分类
            if (searchMap.get("category1Id") != null) {
                criteria.andEqualTo("category1Id", searchMap.get("category1Id"));
            }
            // 二级分类
            if (searchMap.get("category2Id") != null) {
                criteria.andEqualTo("category2Id", searchMap.get("category2Id"));
            }
            // 三级分类
            if (searchMap.get("category3Id") != null) {
                criteria.andEqualTo("category3Id", searchMap.get("category3Id"));
            }
            // 模板ID
            if (searchMap.get("templateId") != null) {
                criteria.andEqualTo("templateId", searchMap.get("templateId"));
            }
            // 运费模板id
            if (searchMap.get("freightId") != null) {
                criteria.andEqualTo("freightId", searchMap.get("freightId"));
            }
            // 销量
            if (searchMap.get("saleNum") != null) {
                criteria.andEqualTo("saleNum", searchMap.get("saleNum"));
            }
            // 评论数
            if (searchMap.get("commentNum") != null) {
                criteria.andEqualTo("commentNum", searchMap.get("commentNum"));
            }
        }
        return example;
    }
}
