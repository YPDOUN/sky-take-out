package com.sky.mapper;


import com.sky.entity.ShoppingCart;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ShoppingCartMapper {

    /**
     * 查询数据库的购物车表中是否存在该数据，并返回购物车对象
     * @param shoppingCart
     * @return
     */
    ShoppingCart queryByShoppingCart(ShoppingCart shoppingCart);

    /**
     * 更新数据
     * @param queryResult
     */
    void update(ShoppingCart queryResult);

    /**
     * 插入数据
     * @param shoppingCart
     */
    void insert(ShoppingCart shoppingCart);

    /**
     * 查询当前用户的购物车数据
     */
    @Select("select * from shopping_cart where user_id = #{userId}")
    List<ShoppingCart> list(Long userId);

    /**
     * 删除单条购物车数据
     * @param queryResult
     */
    void deleteByItem(ShoppingCart queryResult);

    /**
     * 删除当前用户所有购物车数据
     * @param userId
     */
    @Delete("delete from shopping_cart where user_id = #{userId}")
    void deleteByUserId(Long userId);
}
