package com.cao.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cao.reggie.common.BaseContext;
import com.cao.reggie.common.CustomException;
import com.cao.reggie.entity.*;
import com.cao.reggie.mapper.OrdersMapper;
import com.cao.reggie.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private UserService userService;

    @Autowired
    private AddressBookService addressBookService;


    /**
     * 提交订单
     *
     * @param orders
     */
    @Override
    public void submit(Orders orders) {
        //获得当前用户id
        Long currentId = BaseContext.getCurrentId();
        //查询当前用户的购物车数据
        LambdaQueryWrapper<ShoppingCart> shoppingCartWrapper = new LambdaQueryWrapper<>();
        shoppingCartWrapper.eq(ShoppingCart::getUserId, currentId);
        List<ShoppingCart> shoppingCartList = shoppingCartService.list(shoppingCartWrapper);
        //为安全起见检查一下
        if (shoppingCartList == null && shoppingCartList.size() == 0) {
            throw new CustomException("购物车为空,不能下单");
        }
        //查询用户数据
        User user = userService.getById(currentId);
        //查询地址数据,并做校检
        Long addressBookId = orders.getAddressBookId();
        AddressBook addressBook = addressBookService.getById(addressBookId);
        if (addressBook == null) {
            throw new CustomException("用户地址信息有误，不能下单");
        }

        //生成订单ID
        long orderId = IdWorker.getId();

        AtomicInteger amount = new AtomicInteger(0);

        //基于购物车构造订单明细表数据
        List<OrderDetail> orderDetailList = shoppingCartList.stream().map(item -> {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setOrderId(orderId);
            orderDetail.setAmount(item.getAmount());
            orderDetail.setDishFlavor(item.getDishFlavor());
            orderDetail.setDishId(item.getDishId());
            orderDetail.setImage(item.getImage());
            orderDetail.setName(item.getName());
            orderDetail.setNumber(item.getNumber());
            orderDetail.setSetmealId(item.getSetmealId());
            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());
            return orderDetail;
        }).collect(Collectors.toList());

        //构造订单数据
        orders.setId(orderId);
        orders.setAddress(addressBook.getDetail());
        orders.setAmount(new BigDecimal(amount.intValue()));
        orders.setCheckoutTime(LocalDateTime.now());
        orders.setConsignee(addressBook.getConsignee());
        orders.setNumber(String.valueOf(orderId));
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayMethod(2);
        orders.setPhone(addressBook.getPhone());
        orders.setStatus(4);
        orders.setUserId(currentId);
        orders.setUserName(user.getName());

        //向订单表插入数据
        this.save(orders);
        //向订单明细表插入数据
        orderDetailService.saveBatch(orderDetailList);

        //清空购物车数据
        shoppingCartService.remove(shoppingCartWrapper);
    }
}
