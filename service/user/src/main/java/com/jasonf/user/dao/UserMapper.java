package com.jasonf.user.dao;

import com.jasonf.user.pojo.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import tk.mybatis.mapper.common.Mapper;

public interface UserMapper extends Mapper<User> {
    @Update("update tb_user set points = points + #{points} where username = #{username}")
    int updatePoint(@Param("points") Integer points, @Param("username") String userId);
}
