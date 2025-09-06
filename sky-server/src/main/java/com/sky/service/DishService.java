package com.sky.service;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

public interface DishService {

    /**
     * 新增菜品，同时保存对应的口味数据
     * @param dishDTO
     */
    void saveWithFlavor(DishDTO dishDTO);

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 启用禁用菜品
     * @param status
     * @param id
     */
    void dishStartOrStop(Integer status, Long id);

    /**
     * 根据id查询菜品及其口味信息
     * @param id
     * @return
     */
    DishVO getById(Long id);
}
