package com.jasonf.seckill.dao;

import com.jasonf.seckill.pojo.SeckillOrder;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;


public interface SeckillOrderMapper extends Mapper<SeckillOrder> {
    /**
     * 查询已购订单信息
     *
     * @param username
     * @param id
     */
    @Select("select * from tb_seckill_order where user_id = #{username} and seckill_id = #{seckill_id}")
    SeckillOrder queryOrderInfo(@Param("username") String username, @Param("seckill_id") Long id);
}
