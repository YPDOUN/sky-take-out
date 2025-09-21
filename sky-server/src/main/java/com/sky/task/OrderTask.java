package com.sky.task;


import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 定时任务类
 */
@Component
@Slf4j
public class OrderTask {

    @Autowired
    private OrderMapper orderMapper;

    /**
     * 处理订单超时的方法
     */
    @Scheduled(cron = "0 0/1 * * * * ")
    public void processTimeOutOrder() {
        log.info("定时处理超时订单：{}", LocalDateTime.now());

        LocalDateTime time = LocalDateTime.now().minusMinutes(15);
        //select * from order where status = ? and order_time < now - 15
        List<Orders> timeOutOrders = orderMapper.getByStatusAndTimeLT(Orders.PENDING_PAYMENT, time);

        if (timeOutOrders != null && !timeOutOrders.isEmpty()) {
            timeOutOrders.forEach(orders ->{
                orders.setStatus(Orders.CANCELLED);
                orders.setCancelTime(LocalDateTime.now());
                orders.setCancelReason("订单超时，自动取消！");
                orderMapper.update(orders);
                //可改为批量更新
            });
        }
    }

    /**
     * 处理一直处于派送中的订单
     */
    @Scheduled(cron = "0 0 1 * * ? ")
    public void processDeliveryOrder() {
        log.info("处理昨天一直处于派送中的订单{}", LocalDateTime.now());

        LocalDateTime time = LocalDateTime.now().minusMinutes(60);//0点前的订单
        List<Orders> orders = orderMapper.getByStatusAndTimeLT(Orders.DELIVERY_IN_PROGRESS, time);

        if (orders != null && !orders.isEmpty()) {
            orders.forEach(order ->{
                order.setStatus(Orders.COMPLETED);
                orderMapper.update(order);
            });
        }

    }
}
