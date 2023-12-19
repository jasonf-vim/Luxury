package com.jasonf.consume.dao;

import com.jasonf.seckill.pojo.SeckillGoods;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.common.Mapper;

public interface SeckillGoodsMapper extends Mapper<SeckillGoods> {
    @Update("update tb_seckill_goods set stock_count = stock_count - 1 where id = #{goodsId} " +
            "and stock_count >= 1")
    int updateStock(@Param("goodsId") Long seckillId);
}
