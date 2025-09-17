package com.sky.service;

import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;

public interface OrderService {

    /**
     * 用户下单
     */
    OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) throws Exception;

    /**
     * 订单支付
     */
    OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception;

    /**
     * 支付成功，修改订单状态
     */
    void paySuccess(String outTradeNo);

    /**
     * 分页查询用户的历史订单
     */
    PageResult historyOrders(Integer page, Integer pageSize, Integer status);

    /**
     * 查询订单详情
     */
    OrderVO getById(Long id);

    /**
     * 取消订单
     */
    void cancel(Long id) throws Exception;

    /**
     * 再来一单
     */
    void repetition(Long id);


    /**
     * 各个状态的订单数量统计
     */
    PageResult page(OrdersPageQueryDTO ordersPageQueryDTO);

    /**
     * 查询订单详情
     */
    OrderVO getDetailsOfOrder(Long id);

    /**
     * 各个状态的订单数量统计
     */
    OrderStatisticsVO statistics();

    /**
     * 接单
     */
    void confirm(OrdersConfirmDTO ordersConfirmDTO);


    /**
     * 派送订单
     */
    void delivery(Long id);

    /**
     * 拒单
     */
    void rejection(OrdersRejectionDTO ordersRejectionDTO);

    /**
     * 取消订单
     */
    void cancelOrder(OrdersCancelDTO ordersCancelDTO);

    /**
     * 完成订单
     */
    void complete(Long id);
}
