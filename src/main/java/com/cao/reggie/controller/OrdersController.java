package com.cao.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cao.reggie.common.BaseContext;
import com.cao.reggie.common.R;
import com.cao.reggie.dto.OrdersDto;
import com.cao.reggie.entity.OrderDetail;
import com.cao.reggie.entity.Orders;
import com.cao.reggie.service.OrderDetailService;
import com.cao.reggie.service.OrdersService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/order")
public class OrdersController {
    @Autowired
    private OrdersService ordersService;

    @Autowired
    private OrderDetailService orderDetailService;

    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        log.info("提交订单{}",orders.toString());
        ordersService.submit(orders);
        return R.success("提交成功");
    }


    @GetMapping("/userPage")
    public R<Page<OrdersDto>> page(int page, int pageSize){
        Page<Orders> pageInfo=new Page<>();
        LambdaQueryWrapper<Orders> ordersWrapper=new LambdaQueryWrapper<>();
        ordersWrapper.eq(Orders::getUserId, BaseContext.getCurrentId());
        ordersWrapper.orderByDesc(Orders::getOrderTime);
        ordersService.page(pageInfo,ordersWrapper);

        Page<OrdersDto> pageDtoInfo=new Page<>();
        BeanUtils.copyProperties(pageInfo,pageDtoInfo,"records");

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
}
