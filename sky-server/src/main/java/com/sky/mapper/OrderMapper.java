package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.GoodsSalesDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    /**
     * 查询指定状态，并且时间小于time的订单
     */
    @Select("select * from orders where status = #{status} and order_time < #{time}")
    List<Orders> getByStatusAndTimeLT(Integer status, LocalDateTime time);

    /**
     * 查询指定日期的营业额
     */
    @Select("select sum(amount) from orders where order_time between #{begin} and #{end} and status = #{status}")
    Double sumByDate(LocalDateTime begin, LocalDateTime end, Integer status);

    /**
     * 获取指定时间段的用户订单数量
     */
    Integer getOrderCount(Map map);

    /**
     * 查询销量排名top10
     */
    List<GoodsSalesDTO> getSalesTop10(LocalDateTime beginTime, LocalDateTime endTime);

    /**
     * 获取指定时间段内下单的用户数
     */
    Integer userCountByDate(LocalDateTime beginTime, LocalDateTime endTime);

    /**
     * 查询订单总数
     */
    @Select("select count(id) from orders")
    Integer getTotalOrder();
}
