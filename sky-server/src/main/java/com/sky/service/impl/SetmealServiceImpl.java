package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Category;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SetmealServiceImpl implements SetmealService {

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Autowired
    private DishMapper dishMapper;

    /**
     * 新增套餐，同时保存套餐和菜品的关联关系
     *
     * @param setmealDTO
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void save(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        //默认状态为禁用
        setmeal.setStatus(StatusConstant.DISABLE);

        //保存套餐，需要回填主键id
        setmealMapper.insert(setmeal);

        //保存套餐和菜品的关联关系
        List<SetmealDish> setmealDishs = setmealDTO.getSetmealDishes();
        if (setmealDishs != null && !setmealDishs.isEmpty()) {
            //通过回填的主键插入关联的套餐id
            setmealDishs.forEach(
                    setmealDish -> setmealDish.setSetmealId(setmeal.getId())
            );
            //保存关联信息
            setmealDishMapper.insertRelationOfDishAndSetmeal(setmealDishs);
        }
    }

    /**
     * 分页查询
     * @param setmealPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        //TODO 如果分类被禁用，则套餐也不展示
        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());

        Page<SetmealVO> page = setmealMapper.select(setmealPageQueryDTO);

        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 根据id查询套餐
     * @param id
     * @return
     */
    @Override
    public SetmealVO getById(Long id) {

        Setmeal setmeal = setmealMapper.getById(id);
        List<SetmealDish> list = setmealDishMapper.getBySetmealId(id);

        //完成SelmealVO的组装
        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal, setmealVO);
        setmealVO.setSetmealDishes(list);

        return setmealVO;
    }

    /**
     * 套餐起售、停售
     * @param status
     * @param id
     * @return
     */
    @Override
    public void setmealStartOrStop(Integer status, Long id) {
        //TODO【启售/停售】，分类被禁用后，前台不展示

        //此处若是已经起售的套餐且套餐中菜品为禁用，会导致套餐无法停售，解决办法：判断传递的status是否为启售状态
        //套餐内如果有停售菜品，则套餐无法上架
        /*if (status.equals(StatusConstant.ENABLE)) {
            Integer count = setmealDishMapper.countOfDisableDish(id, StatusConstant.DISABLE);
            if (count == null || count > 0) {
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ENABLE_FAILED);
            }
        }*/

        //改进方案：可拓展性更强
        if (status.equals(StatusConstant.ENABLE)) {
            List<Dish> list = dishMapper.getBySetmealId(id);
            if (list != null && list.size() > 0) {
                list.forEach(dish -> {
                    if (dish.getStatus().equals(StatusConstant.DISABLE)) {
                        throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                    }
                });
            }
        }

        Setmeal setmeal = Setmeal.builder()
                .id(id)
                .status(status)
                .build();
        setmealMapper.update(setmeal);
    }

    /**
     * 修改套餐
     * @return
     */
    @Override
    @Transactional(rollbackFor =  Exception.class)
    public void update(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);
        setmealMapper.update(setmeal);
        //更新套餐和菜品的关联关系
        //先删除原有的关联关系
        setmealDishMapper.deleteBySetmealId(setmeal.getId());

        List<SetmealDish> list = setmealDTO.getSetmealDishes();
        if (list != null && !list.isEmpty()) {
            list.forEach(setmealDish -> setmealDish.setSetmealId(setmeal.getId()));
        }
        setmealDishMapper.insertRelationOfDishAndSetmeal(list);
    }

    /**
     * 批量删除套餐
     * @param ids
     */
    @Transactional(rollbackFor =  Exception.class)
    @Override
    public void deleteBatch(List<Long> ids) {

        //在售状态下，不可删除套餐
        Integer count = setmealMapper.countByStatusAndIds(ids, StatusConstant.ENABLE);
        if (count != null && count > 0) {
            throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
        }

        //删除套餐下的菜品关联信息
        setmealDishMapper.deleteBySetmealIds(ids);

        //删除套餐
        setmealMapper.deleteByIds(ids);
    }

    /**
     * 根据分类id查询套餐
     * @param category
     * @return
     */
    public List<Setmeal> getByCategoryId(Category category){
        List<Setmeal> list = setmealMapper.getByCategoryId(category);
        return list;
    }

    /**
     * 根据套餐id查询包含的菜品数据
     * @param id
     * @return
     */
    @Override
    public List<DishItemVO> getBySetmealId(Long id) {
        List<DishItemVO> list = setmealMapper.getBySetmealId(id);
        return list;
    }

}
