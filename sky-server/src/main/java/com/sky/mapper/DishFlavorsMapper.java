package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DishFlavorsMapper {

    /**
     * 保存口味
     * @param flavors
     */
    void insert(List<DishFlavor> flavors);

    /**
     * 根据菜品id删除对应的口味数据
     * @param dishIds
     */
    void deleteByDishIds(List<Long> dishIds);

    /**
     * 根据菜品id删除单个口味数据
     * @param id
     * @return
     */
    void deleteByDishId(Long id);
}
