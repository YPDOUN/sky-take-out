package com.sky.controller.admin;


import com.sky.dto.*;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("adminOrderController")
@RequestMapping("/admin/order")
@Api(tags = "订单管理接口")
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * 各个状态的订单数量统计
     */
    @GetMapping("/conditionSearch")
    @ApiOperation("各个状态的订单数量统计")
    public Result<PageResult> page(OrdersPageQueryDTO ordersPageQueryDTO){
        log.info("订单搜索条件：{}", ordersPageQueryDTO);
        PageResult page = orderService.page(ordersPageQueryDTO);
        return Result.success(page);
    }

    /**
     * 查询订单详情
     */
    @GetMapping("/details/{id}")
    @ApiOperation("查询订单详情")
    public Result<OrderVO> getDetailOfOrder(@PathVariable Long id){
        log.info("查询订单id：{}的信息：", id);
        OrderVO orderVO = orderService.getDetailsOfOrder(id);
        return Result.success(orderVO);
    }

    /**
     * 各个状态的订单数量统计
     */
    @GetMapping("/statistics")
    @ApiOperation("各个状态的订单数量统计")
    public Result<OrderStatisticsVO> statistics() {
        log.info("进行各个状态的订单数量统计");
        OrderStatisticsVO statisticsVO = orderService.statistics();
        return Result.success(statisticsVO);
    }

    /**
     * 接单
     */
    @PutMapping("/confirm")
    @ApiOperation("接单")
    public Result<String> confirm(@RequestBody OrdersConfirmDTO ordersConfirmDTO) {
        log.info("接单");
        orderService.confirm(ordersConfirmDTO);
        return Result.success();
    }

    /**
     * 派送订单
     */
    @PutMapping("/delivery/{id}")
    @ApiOperation("派送订单")
    public Result<String> delivery(@PathVariable Long id) {
        log.info("派送订单");
        orderService.delivery(id);
        return Result.success();
    }

    /**
     * 拒单
     */
    @PutMapping("/rejection")
    @ApiOperation("拒单")
    public Result<String> rejection(@RequestBody OrdersRejectionDTO ordersRejectionDTO) {
        log.info("拒单，订单id及拒单原因：{}",ordersRejectionDTO);
        orderService.rejection(ordersRejectionDTO);
        return Result.success();
    }

    /**
     * 取消订单
     */
    @PutMapping("/cancel")
    @ApiOperation("取消订单")
    public Result<String> cancel(@RequestBody OrdersCancelDTO ordersCancelDTO) {
        log.info("取消订单，订单id及取消原因：{}", ordersCancelDTO);
        orderService.cancelOrder(ordersCancelDTO);
        return Result.success();
    }

    /**
     * 完成订单
     */
    @PutMapping("/complete/{id}")
    @ApiOperation("完成订单")
    public Result<String> complete(@PathVariable Long id) {
        log.info("完成订单：{}", id);
        orderService.complete(id);
        return Result.success();
    }
}
