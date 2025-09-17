package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Mapper
public interface OrderMapper {

    /**
     * 插入订单
     */
    void insert(Orders orders);

    /**
     * 根据订单号查询订单
     */
    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    /**
     * 修改订单信息
     */
    void update(Orders orders);

    /**
     * 通过userId获取历史订单
     */
    Page<OrderVO> pageOfHistory(Orders orders);

    /**
     * 根据订单id获取数据
     */
    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    /**
     * 各个状态的订单数量统计
     */
    Page<Orders> page(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 各个状态的订单数量统计
     */
    @Select("select count(*) from orders where status = #{status}")
    Integer getStatisticsByStatus(Integer status);
}
