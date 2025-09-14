package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Category;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealMapper {


    /**
     * 查询关联了该分类的套餐数量
     * @param id
     * @return
     */
    @Select("select count(*) from setmeal where category_id = #{id}")
    Integer countByCategoryId(Long id);


    /**
     * 保存套餐信息
     * @param setmeal
     */
    @AutoFill(OperationType.INSERT)
    void insert(Setmeal setmeal);

    /**
     * 分页查询
     *
     * @param setmealPageQueryDTO
     * @return
     */
    Page<SetmealVO> select(SetmealPageQueryDTO setmealPageQueryDTO);

    /**
     * 根据id查询套餐
     * @param id
     * @return
     */
    @Select("select * from setmeal where id = #{id}")
    Setmeal getById(Long id);

    /**
     * 更新套餐
     * @param setmeal
     */
    @AutoFill(OperationType.UPDATE)
    void update(Setmeal setmeal);

    /**
     * 返回集合ids中，状态为起售或者停售的套餐数量
     * @param ids
     * @param status
     * @return
     */
    Integer countByStatusAndIds(List<Long> ids, Integer status);

    /**
     * 根据ids批量删除套餐
     * @param ids
     */
    void deleteByIds(List<Long> ids);

    /**
     * 根据分类id查询套餐
     * @param category
     * @return
     */
    @Select("select * from setmeal where category_id = #{id}")
    List<Setmeal> getByCategoryId(Category category);

    /**
     * 根据套餐id查询菜品选项
     * @param setmealId
     * @return
     */
    @Select("select d.name, sd.copies, d.image, d.description from setmeal s " +
            "left join setmeal_dish sd on s.id = sd.setmeal_id " +
            "left join dish d on sd.dish_id = d.id " +
            "where s.id = #{setmealId}")
    List<DishItemVO> getBySetmealId(Long setmealId);
}
