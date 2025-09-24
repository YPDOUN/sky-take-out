package com.sky.service.impl;

import com.alibaba.druid.support.spring.stat.annotation.Stat;
import com.sky.constant.StatusConstant;
import com.sky.entity.Orders;
import com.sky.mapper.DishMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.WorkspaceService;
import com.sky.vo.BusinessDataVO;
import com.sky.vo.DishOverViewVO;
import com.sky.vo.OrderOverViewVO;
import com.sky.vo.SetmealOverViewVO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class WorkspaceServiceImpl implements WorkspaceService {


    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private DishMapper dishMapper;

    /**
     * 查询今日运营数据
     */
    @Override
    public BusinessDataVO getBusinessData() {
        LocalDateTime beginTime = LocalDateTime.now().with(LocalTime.MIN); //今天时间重置到0点
        LocalDateTime endTime = LocalDateTime.of(LocalDate.now(), LocalTime.MAX); //今天日期拼接上最大时间

        //新增用户数
        Integer newUsers = userMapper.getUsersByTime(beginTime, endTime);
        //有效订单数
        Integer validOrders = getOrderCount(beginTime, endTime, Orders.COMPLETED);
        //总订单数
        Integer totalOrders = getOrderCount(beginTime, endTime, null);
        //完成率
        Double orderCompletionRate = totalOrders == 0 ? 0.0 : validOrders.doubleValue() / totalOrders;
        //营业额
        Double turnover = orderMapper.sumByDate(beginTime, endTime, Orders.COMPLETED);
        turnover = turnover == null ? 0.0 : turnover;
        //平均客单价 = 营业额 / 当天下单用户数量
        Integer todayUsers = orderMapper.userCountByDate(beginTime, endTime);
        Double unitPrice = todayUsers == 0 ? 0.0 : turnover / todayUsers;

        return BusinessDataVO.builder()
                .newUsers(newUsers)
                .validOrderCount(validOrders)
                .orderCompletionRate(orderCompletionRate)
                .turnover(turnover)
                .unitPrice(unitPrice)
                .build();
    }

    /**
     * 查询套餐总览
     */
    @Override
    public SetmealOverViewVO getOverviewSetmeals() {
        Integer sold = setmealMapper.getOverviewOfSetmeal(StatusConstant.ENABLE);
        Integer discontinued = setmealMapper.getOverviewOfSetmeal(StatusConstant.DISABLE);
        return SetmealOverViewVO.builder()
                .sold(sold)
                .discontinued(discontinued)
                .build();
    }

    /**
     * 查询菜品总览
     */
    @Override
    public DishOverViewVO getOverviewDishes() {
        Integer sold = dishMapper.getOverviewOfDish(StatusConstant.ENABLE);
        Integer discontinued = dishMapper.getOverviewOfDish(StatusConstant.DISABLE);
        return DishOverViewVO.builder()
                .sold(sold)
                .discontinued(discontinued)
                .build();
    }

    /**
     * 查询订单管理数据
     */
    @Override
    public OrderOverViewVO getOverviewOrders() {
        Integer totalOrders = orderMapper.getTotalOrder();
        Integer cancelledOrders = orderMapper.getStatisticsByStatus(Orders.CANCELLED);
        Integer completedOrders = orderMapper.getStatisticsByStatus(Orders.COMPLETED);
        Integer deliveredOrders = orderMapper.getStatisticsByStatus(Orders.DELIVERY_IN_PROGRESS);
        Integer waitingOrders = orderMapper.getStatisticsByStatus(Orders.TO_BE_CONFIRMED);
        return OrderOverViewVO.builder()
                .allOrders(totalOrders)
                .cancelledOrders(cancelledOrders)
                .completedOrders(completedOrders)
                .deliveredOrders(deliveredOrders)
                .waitingOrders(waitingOrders)
                .build();
    }

    /**
     * 查询指定时间区间的订单数，以及可指定某一状态
     * 封装map便于拓展
     */
    private Integer getOrderCount(LocalDateTime begin, LocalDateTime end, Integer status) {
        Map map = new HashMap();
        map.put("beginTime", begin);
        map.put("endTime", end);
        map.put("status", status);
        return orderMapper.getOrderCount(map);
    }

}
