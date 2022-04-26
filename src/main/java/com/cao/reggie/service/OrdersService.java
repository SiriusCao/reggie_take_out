package com.cao.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cao.reggie.entity.Orders;

public interface OrdersService extends IService<Orders> {

    public void submit(Orders orders);

}
