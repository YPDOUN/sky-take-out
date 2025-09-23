package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;

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

    /**
     * 根据用户id查询信息
     * @param userId
     */
    @Select("select * from user where id = #{userId}")
    User getById(Long userId);

    /**
     * 统计指定时间段的用户数
     */
    Integer getUsersByTime(LocalDateTime begin, LocalDateTime end);
}
