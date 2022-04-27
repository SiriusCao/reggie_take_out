package com.cao.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cao.reggie.common.BaseContext;
import com.cao.reggie.common.R;
import com.cao.reggie.dto.OrdersDto;
import com.cao.reggie.entity.OrderDetail;
import com.cao.reggie.entity.OrderMap;
import com.cao.reggie.entity.Orders;
import com.cao.reggie.entity.ShoppingCart;
import com.cao.reggie.service.OrderDetailService;
import com.cao.reggie.service.OrdersService;
import com.cao.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/order")
public class OrdersController {
    @Autowired
    private OrdersService ordersService;

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private ShoppingCartService shoppingCartService;

    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders) {
        log.info("提交订单{}", orders.toString());
        ordersService.submit(orders);
        return R.success("提交成功");
    }


    @GetMapping("/userPage")
    public R<Page<OrdersDto>> page(int page, int pageSize) {
        Page<Orders> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Orders> ordersWrapper = new LambdaQueryWrapper<>();
        ordersWrapper.eq(Orders::getUserId, BaseContext.getCurrentId());
        ordersWrapper.orderByDesc(Orders::getOrderTime);
        ordersService.page(pageInfo, ordersWrapper);

        Page<OrdersDto> pageDtoInfo = new Page<>();
        BeanUtils.copyProperties(pageInfo, pageDtoInfo, "records");

        List<Orders> ordersList = pageInfo.getRecords();

        List<OrdersDto> ordersDtoList = ordersList.stream().map(item -> {
            OrdersDto ordersDto = new OrdersDto();
            BeanUtils.copyProperties(item, ordersDto);
            LambdaQueryWrapper<OrderDetail> orderDetailWrapper = new LambdaQueryWrapper<>();
            orderDetailWrapper.eq(OrderDetail::getOrderId, item.getId());
            List<OrderDetail> orderDetailList = orderDetailService.list(orderDetailWrapper);
            ordersDto.setOrderDetails(orderDetailList);
            return ordersDto;
        }).collect(Collectors.toList());

        pageDtoInfo.setRecords(ordersDtoList);
        return R.success(pageDtoInfo);
    }

    /**
     * 再次下单
     *
     * @param orders 包含需要重复下单的订单ID
     * @return
     */
    @PostMapping("/again")
    public R<String> againOrder(@RequestBody Orders orders) {
        //获取要重复下单的订单号
        Long orderId = orders.getId();
        //根据订单号获取订单内容
        LambdaQueryWrapper<OrderDetail> orderDetailWrapper = new LambdaQueryWrapper<>();
        orderDetailWrapper.eq(OrderDetail::getOrderId, orderId);
        List<OrderDetail> orderDetailList = orderDetailService.list(orderDetailWrapper);
        //根据订单内容查询到dishID或者setmealId并将其对应的其他信息再次加入购物车
        List<ShoppingCart> shoppingCartList = orderDetailList.stream().map(item -> {
            ShoppingCart shoppingCart = new ShoppingCart();
            //如果是普通菜品
            if (item.getDishId() != null) {
                shoppingCart.setName(item.getName());
                shoppingCart.setImage(item.getImage());
                shoppingCart.setUserId(BaseContext.getCurrentId());
                shoppingCart.setDishId(item.getDishId());
                shoppingCart.setDishFlavor(item.getDishFlavor());
                shoppingCart.setNumber(item.getNumber());
                shoppingCart.setAmount(item.getAmount());
                shoppingCart.setCreateTime(LocalDateTime.now());
            } else if (item.getSetmealId() != null) {
                //如果是套餐
                shoppingCart.setName(item.getName());
                shoppingCart.setImage(item.getImage());
                shoppingCart.setUserId(BaseContext.getCurrentId());
                shoppingCart.setSetmealId(item.getDishId());
                shoppingCart.setNumber(item.getNumber());
                shoppingCart.setAmount(item.getAmount());
                shoppingCart.setCreateTime(LocalDateTime.now());
            }
            return shoppingCart;
        }).collect(Collectors.toList());
        boolean b = shoppingCartService.saveBatch(shoppingCartList);
        if (b) {
            return R.success("");
        }
        return R.error("未知错误");
    }

    @GetMapping("/page")
    public R<Page<OrdersDto>> page(OrderMap orderMap) {
        Integer page = orderMap.getPage();
        Integer pageSize = orderMap.getPageSize();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        LocalDateTime beginTime = null;
        LocalDateTime endTime = null;

        if (orderMap.getBeginTime() != null) {
            beginTime = LocalDateTime.parse(orderMap.getBeginTime(), formatter);
        }
        if (orderMap.getEndTime() != null) {
            endTime = LocalDateTime.parse(orderMap.getEndTime(), formatter);
        }

        String number = orderMap.getNumber();

        Page<Orders> pageInfo = new Page<>(page, pageSize);
        LambdaQueryWrapper<Orders> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(number != null, Orders::getNumber, number);
        wrapper.ge(beginTime != null, Orders::getOrderTime, beginTime);
        wrapper.le(endTime != null, Orders::getOrderTime, endTime);
        wrapper.orderByDesc(Orders::getOrderTime);

        ordersService.page(pageInfo, wrapper);

        Page<OrdersDto> ordersDtoPageInfo=new Page<>();
        BeanUtils.copyProperties(pageInfo,ordersDtoPageInfo,"records");

        List<Orders> ordersList = pageInfo.getRecords();
        List<OrdersDto> ordersDtoList = ordersList.stream().map(item -> {
            OrdersDto ordersDto = new OrdersDto();
            BeanUtils.copyProperties(item, ordersDto);
            LambdaQueryWrapper<OrderDetail> wrapper1 = new LambdaQueryWrapper<>();
            wrapper1.eq(OrderDetail::getOrderId, item.getNumber());
            List<OrderDetail> orderDetails = orderDetailService.list(wrapper1);
            ordersDto.setOrderDetails(orderDetails);
            return ordersDto;
        }).collect(Collectors.toList());

        ordersDtoPageInfo.setRecords(ordersDtoList);

        return R.success(ordersDtoPageInfo);
    }
}
