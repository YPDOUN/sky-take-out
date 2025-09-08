package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    /**
     * 根据菜品ids查询对应的套餐id
     * @param ids
     * @return
     */
    List<Long> getSetmealIdByDishIds(List<Long> ids);

    /**
     * 插入菜品和套餐的关系
     * @param setmealDishs
     */
    void insertRelationOfDishAndSetmeal(List<SetmealDish> setmealDishs);

    /**
     * 通过套餐id获取对应的菜品集合
     * @param id
     * @return
     */
    @Select("select * from setmeal_dish where setmeal_id = #{id}")
    List<SetmealDish> getBySetmealId(Long id);

    /**
     * 查询待起售套餐中，未起售状态的菜品数量
     * @param status
     * @param setmealId
     */
    Integer countOfDisableDish(Long setmealId, Integer status);

    /**
     * 根据套餐id删除对应的菜品关系
     * @param id
     */
    @Delete("delete from setmeal_dish where setmeal_id = #{id}")
    void deleteBySetmealId(Long id);

    /**
     * 根据套餐id集合删除对应的菜品关系
     * @param ids
     */
    void deleteBySetmealIds(List<Long> ids);
}
