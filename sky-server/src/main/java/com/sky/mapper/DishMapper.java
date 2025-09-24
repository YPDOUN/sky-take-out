package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DishMapper {

    /**
     * 查询关联了该分类的菜品数量
     * @param id
     * @return
     */
    @Select("select count(*) from dish where category_id = #{id}")
    Integer countByCategoryId(Long id);

    /**
     * 新增菜品
     * @param dish
     */
    @AutoFill(OperationType.INSERT)
    void insert(Dish dish);

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    Page<DishVO> pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 更新菜品信息
     * @param dish
     * @return
     */
    @AutoFill(OperationType.UPDATE)
    void update(Dish dish);

    /**
     * 根据id查询菜品及其口味信息
     * @param id
     * @return
     */
    @Select("select * from dish where id = #{id}")
    Dish getById(Long id);

    /**
     * 返回集合ids中，状态为status的菜品数量
     * @param ids
     */
    Integer countByIdsAndStatus(List<Long> ids, Integer status);

    /**
     * 批量删除菜品
     * @param ids
     */
    void deleteByIds(List<Long> ids);

    /**
     * 根据分类id查询菜品
     * @return
     */
    @Select("select * from dish where category_id = #{id}")
    List<Dish> getByCategoryId(Long id);

    /**
     * 根据套餐id查询对应的菜品
     * @param id
     * @return
     */
    List<Dish> getBySetmealId(Long id);

    /**
     * 查询菜品总览
     */
    @Select("select count(*) from dish where status = #{status}")
    Integer getOverviewOfDish(Integer status);
}
