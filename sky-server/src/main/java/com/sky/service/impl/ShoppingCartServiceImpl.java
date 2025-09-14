package com.sky.service.impl;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;


@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private SetmealMapper setmealMapper;

    /**
     * 添加购物车
     * @param shoppingCartDTO
     */
    @Override
    public void add(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart cart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, cart);
        cart.setUserId(BaseContext.getCurrentId());

        // 判断数据库中是否存在该菜品或套餐，如果存在则数量加1，不存在则添加
        ShoppingCart queryResult = shoppingCartMapper.queryByShoppingCart(cart);
        if (queryResult != null) {
            queryResult.setNumber(queryResult.getNumber() + 1);
            shoppingCartMapper.update(queryResult);
        } else {
            cart.setNumber(1); //默认添加1份
            cart.setCreateTime(LocalDateTime.now());

            if (cart.getDishId() != null) {
                Dish dish = dishMapper.getById(cart.getDishId());
                cart.setName(dish.getName());
                cart.setImage(dish.getImage());
                cart.setAmount(dish.getPrice());
            } else {
                Setmeal setmeal = setmealMapper.getById(cart.getSetmealId());
                cart.setName(setmeal.getName());
                cart.setImage(setmeal.getImage());
                cart.setAmount(setmeal.getPrice());
            }
            shoppingCartMapper.insert(cart);
        }
    }

    /**
     * 查看购物车
     * @return
     */
    @Override
    public List<ShoppingCart> list() {
        Long userId = BaseContext.getCurrentId();
        return shoppingCartMapper.list(userId);
    }

    /**
     * 删除购物车中一个商品
     * @param shoppingCartDTO
     */
    @Override
    public void sub(ShoppingCartDTO shoppingCartDTO) {
        ShoppingCart cart = new ShoppingCart();
        BeanUtils.copyProperties(shoppingCartDTO, cart);
        cart.setUserId(BaseContext.getCurrentId());

        ShoppingCart queryResult = shoppingCartMapper.queryByShoppingCart(cart);
        if (queryResult != null && queryResult.getNumber() > 1) {
            queryResult.setNumber(queryResult.getNumber() - 1);
            shoppingCartMapper.update(queryResult);
        } else if (queryResult != null && queryResult.getNumber() == 1) {
            shoppingCartMapper.deleteByItem(queryResult);
        }
    }

    @Override
    public void clean() {
        Long userId = BaseContext.getCurrentId();
        shoppingCartMapper.deleteByUserId(userId);
    }
}
