package com.cao.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cao.reggie.dto.SetmealDto;
import com.cao.reggie.entity.Setmeal;

public interface SetmealService extends IService<Setmeal> {

    //新增套餐，和其中的菜品
    public void saveWithDish(SetmealDto setmealDto);
}
