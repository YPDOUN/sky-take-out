package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {


    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;
    
    /**
     * 营业额统计
     */
    @Override
    public TurnoverReportVO turnoverStatistics(LocalDate begin, LocalDate end) {

        List<LocalDate> dateList = new ArrayList<>();
        List<Double> turnoverList = new ArrayList<>();

        //生成日期列表
        while(!begin.isAfter(end)) {
            dateList.add(begin);
            begin = begin.plusDays(1);
        }

        dateList.forEach(date->{
            //封装为LocalDateTime
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            Double turnover = orderMapper.sumByDate(beginTime, endTime, Orders.COMPLETED);
            turnover = turnover == null ? 0.0 : turnover;
            turnoverList.add(turnover);
        });

        return TurnoverReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .turnoverList(StringUtils.join(turnoverList, ","))
                .build();
    }

    /**
     * 用户统计接口
     */
    @Override
    public UserReportVO userStatistics(LocalDate begin, LocalDate end) {

        List<LocalDate> dateList = new ArrayList<>();
        List<Integer> newUserList = new ArrayList<>();
        List<Integer> totalUserList = new ArrayList<>();
        
        while (!begin.isAfter(end)) {
            dateList.add(begin);
            begin = begin.plusDays(1);
        }

        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            //查询当天注册的用户数 create_time > begin and create_time < end
            Integer newUsersAmount = userMapper.getUsersByTime(beginTime, endTime);
            //查询总的用户数 create_time < end
            Integer totalUser = userMapper.getUsersByTime(null, endTime);
            newUserList.add(newUsersAmount);
            totalUserList.add(totalUser);
        }

        return UserReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .newUserList(StringUtils.join(newUserList, ","))
                .totalUserList(StringUtils.join(totalUserList, ","))
                .build();
    }

    /**
     * 订单统计接口
     */
    @Override
    public OrderReportVO ordersStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();

        while (!begin.isAfter(end)) {
            dateList.add(begin);
            begin = begin.plusDays(1);
        }

        for (LocalDate date : dateList) {
            LocalDateTime beginTime = LocalDateTime.of(date, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(date, LocalTime.MAX);

            //查询[begin,end]的有效订单数，status=5
            Integer validOrders = getOrderCount(beginTime, endTime, Orders.COMPLETED);
            validOrderCountList.add(validOrders);
            //查询[begin,end]的订单总数
            Integer orderCounts = getOrderCount(beginTime, endTime, null);
            orderCountList.add(orderCounts);
        }

        //计算有效订单总量
        Integer validOrderCount = validOrderCountList.stream().reduce(Integer::sum).get();
        //计算订单总量
        Integer totalOrderCount = orderCountList.stream().reduce(Integer::sum).get();
        //订单完成率
        Double orderCompletionRate = totalOrderCount == 0 ? 0.0 : validOrderCount.doubleValue() / totalOrderCount;

        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCount(validOrderCount)
                .totalOrderCount(totalOrderCount)
                .orderCompletionRate(orderCompletionRate)
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

    /**
     * 查询销量排名top10
     */
    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {

        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MIN);

        List<GoodsSalesDTO> salesTop10 = orderMapper.getSalesTop10(beginTime, endTime);

        List<String> name = salesTop10.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        List<Integer> number = salesTop10.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());

        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(name, ","))
                .numberList(StringUtils.join(number, ","))
                .build();

    }
}
