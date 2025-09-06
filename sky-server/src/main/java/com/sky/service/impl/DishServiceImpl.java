package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorsMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorsMapper dishFlavorsMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;

    /**
     * 新增菜品，同时保存对应的口味数据
     *
     * @param dishDTO
     */
    @Transactional(rollbackFor =  Exception.class)
    @Override
    public void saveWithFlavor(DishDTO dishDTO) {
        //拷贝属性到实体类
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        //设置默认启用状态为禁用
        dish.setStatus(StatusConstant.DISABLE);
        //保存菜品，需要设置useGeneratedKeys="true" keyProperty="id"用于主键回填
        dishMapper.insert(dish);

        //获取口味
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && !flavors.isEmpty()) {
            //设置口味对应的菜品id
            flavors.forEach(flavor -> flavor.setDishId(dish.getId()));
            //保存口味
            dishFlavorsMapper.insert(flavors);
        }
    }

    /**
     * 菜品分页查询
     *
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 启用禁用菜品
     *
     * @param status
     * @param id
     */
    @Override
    public void dishStartOrStop(Integer status, Long id) {
        Dish dish = Dish.builder()
                .id(id)
                .status(status)
                .build();
        dishMapper.update(dish);
    }


    /**
     * 根据id查询菜品及其口味信息
     *
     * @param id
     * @return
     */
    @Override
    public DishVO getById(Long id) {
        DishVO dishVO = dishMapper.getById(id);
        return dishVO;
    }

    /**
     * 批量删除菜品
     *
     * @param ids
     */
    @Transactional(rollbackFor =  Exception.class)
    @Override
    public void deleteBatch(List<Long> ids) {

        //在售状态下，不可删除菜品; 被套餐关联的菜品不能删除
        if (ids == null || ids.isEmpty()) {
            return;
        }

        //查询判断当前菜品是否在售，在售则不能删除
        Integer count = dishMapper.countByIdsAndStatus(ids, StatusConstant.ENABLE);
        if (count != null && count > 0) {
            throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
        }
        //判断是否被套餐关联，有关联则不能删除
        List<Long> setmealId = setmealDishMapper.getSetmealIdByDishIds(ids);
        if (setmealId != null && setmealId.size() > 0) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        //以下则可正常删除
        //先删除口味数据
        dishFlavorsMapper.deleteByDishIds(ids);
        //删除菜品数据
        dishMapper.deleteByIds(ids);
    }
}
