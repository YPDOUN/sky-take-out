package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {

    /**
     * 根据openid查询用户
     * @param openId
     * @return
     */
    @Select("select * from user where openid = #{openId}")
    User getByOpenid(String openId);

    /**
     * 插入用户数据，并回填主键
     * @param user
     */
    void insert(User user);


}
