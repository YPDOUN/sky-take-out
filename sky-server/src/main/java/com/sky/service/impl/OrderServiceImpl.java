package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.*;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.BaiduUtil;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.math.BigDecimal;
import com.sky.exception.OrderBusinessException;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private AddressBookMapper addressBookMapper;
    @Autowired
    private ShoppingCartMapper shoppingCartMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WeChatPayUtil weChatPayUtil;
    @Autowired
    private BaiduUtil baiduUtil;

    /**
     * 用户下单
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) throws Exception {

        //处理业务异常：购物车是否有数据、地址簿为空
        Long userId = BaseContext.getCurrentId();

        AddressBook addressBook = addressBookMapper.getById(ordersSubmitDTO.getAddressBookId());
        if (addressBook == null) {
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(userId);
        if (shoppingCartList == null || shoppingCartList.isEmpty()) {
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        //如果距离大于5公里抛出异常
        String address = addressBook.getProvinceName()+ addressBook.getCityName() +
                addressBook.getDistrictName() + addressBook.getDetail();
        if (baiduUtil.getDistance(address) > 5) {
            throw new OrderBusinessException(MessageConstant.DISTANCE_LIMIT_EXCEEDED_MESSAGE);
        }

        //向订单表orders插入1条数据
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO, orders);
        orders.setNumber(String.valueOf(System.currentTimeMillis())); //订单号
        orders.setStatus(Orders.PENDING_PAYMENT); //订单状态：未付款
        orders.setUserId(userId); //用户id
        orders.setOrderTime(LocalDateTime.now()); //下单时间
        orders.setPayMethod(1); //微信
        orders.setPayStatus(Orders.UN_PAID); //付款状态：未付款
        orders.setPhone(addressBook.getPhone()); //用户手机号
        orders.setAddress(address); //用户地址
        orders.setConsignee(addressBook.getConsignee()); //收货人

        orderMapper.insert(orders);

        //向订单详情表order_detail插入多条数据
        List<OrderDetail> detailList = new ArrayList<>();

        shoppingCartList.forEach(list -> {
            OrderDetail detail = new OrderDetail();
            BeanUtils.copyProperties(list, detail);
            detail.setOrderId(orders.getId());
            detailList.add(detail);
        });
        orderDetailMapper.insertBatch(detailList);
        //清空购物车数据
        shoppingCartMapper.deleteByUserId(userId);
        //构造VO对象返回前端
        return new OrderSubmitVO(orders.getId(), orders.getNumber(), orders.getAmount(), orders.getOrderTime());
    }


    /**
     * 订单支付
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
        JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        return vo;
    }

    /**
     * 支付成功，修改订单状态
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }

    /**
     * 分页查询用户的历史订单
     */
    @Override
    public PageResult historyOrders(Integer page, Integer pageSize, Integer status) {

        PageHelper.startPage(page, pageSize);

        Orders orders = new Orders();
        orders.setUserId(BaseContext.getCurrentId());
        orders.setStatus(status);

        Page<OrderVO> pages = orderMapper.pageOfHistory(orders);

        pages.forEach(order -> {
            List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(order.getId());
            order.setOrderDetailList(orderDetails);

        });

        return new PageResult(pages.getTotal(), pages.getResult());
    }

    /**
     * 根据订单id获取数据
     */
    @Override
    public OrderVO getById(Long id) {

        //分别查询订单数据和对应的订单详细信息
        Orders orders = orderMapper.getById(id);
        List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(id);

        //封装为OrderVO对象
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        orderVO.setOrderDetailList(orderDetails);

        return orderVO;
    }

    /**
     * 取消订单
     */
    @Override
    public void cancel(Long id) throws Exception {

        //修改订单状态为取消状态
        Orders orders = orderMapper.getById(id);

        //检验订单是否存在
        if (orders == null) {
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        if (orders.getStatus() > 2) {
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        //未接单则退款
        if (orders.getStatus().equals(Orders.TO_BE_CONFIRMED)) {
            //调用wx接口退款
            weChatPayUtil.refund(
                    orders.getNumber(), //商户订单号
                    orders.getNumber(), //商户退款单号
                    new BigDecimal("0.01"), //退款金额
                    new BigDecimal("0.01")); //原金额
            orders.setPayStatus(Orders.REFUND);
        }

        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason("用户取消");
        orders.setCancelTime(LocalDateTime.now());
        orderMapper.update(orders);
    }

    /**
     * 再来一单
     */
    @Override
    public void repetition(Long id) {
        //商品写回购物车

        //获取用户id
        Long userId = BaseContext.getCurrentId();

        //获取id关联的订单明细
        List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(id);

        orderDetails.forEach(orderDetail -> {
            ShoppingCart cart = new ShoppingCart();
            BeanUtils.copyProperties(orderDetail, cart);
            cart.setUserId(userId);

            shoppingCartMapper.insert(cart);
        });
    }

    /**
     * 各个状态的订单数量统计
     */
    @Override
    public PageResult page(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Page<Orders> page = orderMapper.page(ordersPageQueryDTO);
        return  new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 查询订单详情
     */
    @Override
    public OrderVO getDetailsOfOrder(Long id) {

        //查询订单和相关的订单详情
        Orders orders = orderMapper.getById(id);
        List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(orders.getId());

        //组装为VO对象
        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders, orderVO);
        orderVO.setOrderDetailList(orderDetails);

        return orderVO;
    }

    /**
     * 各个状态的订单数量统计
     */
    @Override
    public OrderStatisticsVO statistics() {
        Integer toBeConfirmed = orderMapper.getStatisticsByStatus(Orders.TO_BE_CONFIRMED);
        Integer confirmed = orderMapper.getStatisticsByStatus(Orders.CONFIRMED);
        Integer deliveryInProgress = orderMapper.getStatisticsByStatus(Orders.DELIVERY_IN_PROGRESS);

        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        orderStatisticsVO.setConfirmed(confirmed);
        orderStatisticsVO.setDeliveryInProgress(deliveryInProgress);
        return orderStatisticsVO;
    }

    /**
     * 接单
     */
    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders = new Orders();
        orders.setId(ordersConfirmDTO.getId());
        orders.setStatus(Orders.CONFIRMED);

        orderMapper.update(orders);
    }

    /**
     * 派送订单
     */
    @Override
    public void delivery(Long id) {
        Orders orders = Orders.builder()
                .id(id)
                .status(Orders.DELIVERY_IN_PROGRESS)
                .deliveryTime(LocalDateTime.now())
                .build();
        orderMapper.update(orders);
    }

    /**
     * 拒单
     */
    @Override
    public void rejection(OrdersRejectionDTO ordersRejectionDTO) {
        Orders orders = Orders.builder()
                .id(ordersRejectionDTO.getId())
                .rejectionReason(ordersRejectionDTO.getRejectionReason())
                .cancelTime(LocalDateTime.now())
                .status(Orders.CANCELLED)
                .build();
        orderMapper.update(orders);
    }

    /**
     * 取消订单
     */
    @Override
    public void cancelOrder(OrdersCancelDTO ordersCancelDTO) {
        Orders orders = Orders.builder()
                .id(ordersCancelDTO.getId())
                .cancelReason(ordersCancelDTO.getCancelReason())
                .cancelTime(LocalDateTime.now())
                .status(Orders.CANCELLED)
                .build();
        orderMapper.update(orders);
    }

    /**
     * 完成订单
     */
    @Override
    public void complete(Long id) {
        Orders orders = Orders.builder()
                .id(id)
                .status(Orders.COMPLETED)
                .build();
        orderMapper.update(orders);
    }


}