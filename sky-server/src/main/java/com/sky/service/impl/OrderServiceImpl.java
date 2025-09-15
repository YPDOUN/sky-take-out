package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.AddressBook;
import com.sky.entity.OrderDetail;
import com.sky.entity.Orders;
import com.sky.entity.ShoppingCart;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.AddressBookMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.OrderService;
import com.sky.vo.OrderSubmitVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    /**
     * 用户下单
     * @param ordersSubmitDTO
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {

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
        String address = addressBook.getProvinceName()+ addressBook.getCityName() +
                addressBook.getDistrictName() + addressBook.getDetail();
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
}