package com.cao.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cao.reggie.dto.SetmealDto;
import com.cao.reggie.entity.Setmeal;
import com.cao.reggie.entity.SetmealDish;
import com.cao.reggie.mapper.SetmealMapper;
import com.cao.reggie.service.SetmealDishService;
import com.cao.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {
    @Autowired
    private SetmealDishService setmealDishService;

    @Override
    public void saveWithDish(SetmealDto setmealDto) {
        //新增套餐
        this.save(setmealDto);

        //获得传回的菜品信息
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        //向setmealDishes当中插入setmealID
        setmealDishes = setmealDishes.stream().map(item -> {
            item.setSetmealId(setmealDto.getId());
            return item;
        }).collect(Collectors.toList());

        //插入套餐菜品信息
        setmealDishService.saveBatch(setmealDishes);
    }
}
