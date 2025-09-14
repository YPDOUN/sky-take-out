package com.sky.controller.user;


import com.sky.entity.Category;
import com.sky.entity.Setmeal;
import com.sky.result.Result;
import com.sky.service.SetmealService;
import com.sky.vo.DishItemVO;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("userSetmealController")
@RequestMapping("user/setmeal")
@Api(tags = "C端-套餐浏览接口")
@Slf4j
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    /**
     * 根据分类id查询套餐
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询套餐")
    @Cacheable(cacheNames = "setmealCache", key = "#categoryId") //key: setmeal::categoryId
    public Result<List<Setmeal>> getById(Long categoryId){
        log.info("根据分类id查询套餐：{}", categoryId);

        Category category = Category.builder().id(categoryId).build();

        List<Setmeal> list = setmealService.getByCategoryId(category);

        return Result.success(list);
    }

    @GetMapping("/dish/{id}")
    @ApiOperation("根据套餐id查询包含的菜品")
    public Result<List<DishItemVO>> getBySetmealId(@PathVariable Long id){
        log.info("查询套餐下的菜品：{}", id);
        List<DishItemVO> list = setmealService.getBySetmealId(id);
        return Result.success(list);
    }
}
