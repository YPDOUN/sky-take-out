package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
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
    SetmealVO getById(Long id);

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
}
