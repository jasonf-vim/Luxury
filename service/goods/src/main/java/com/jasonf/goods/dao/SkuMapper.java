package com.jasonf.goods.dao;

import com.jasonf.goods.pojo.Sku;
import com.jasonf.order.pojo.OrderItem;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.common.Mapper;

public interface SkuMapper extends Mapper<Sku> {
    @Update("update tb_sku set num = num - #{num}, sale_num = sale_num + #{num} where id = #{skuId} and num >= #{num}")
    int decrCount(OrderItem orderItem);

    // 回滚操作
    @Update("update tb_sku set num = num + #{num}, sale_num = sale_num - #{num} where id = #{skuId}")
    void rollback(@Param("skuId") String skuId, @Param("num") Integer num);
}
