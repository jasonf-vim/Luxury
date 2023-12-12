package com.jasonf.goods.dao;

import com.jasonf.goods.pojo.Spec;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;
import java.util.Map;

public interface SpecMapper extends Mapper<Spec> {
    // 根据商品分类名称查询规格列表
    @Select("select name, options from tb_spec where template_id in ( select template_id from tb_category where name = #{categoryName} ) order by seq")
    List<Map> findByCategoryName(@Param("categoryName") String categoryName);
}
