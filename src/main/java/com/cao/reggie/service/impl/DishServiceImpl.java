package com.cao.reggie.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cao.reggie.dto.DishDto;
import com.cao.reggie.entity.Dish;
import com.cao.reggie.entity.DishFlavor;
import com.cao.reggie.mapper.DishMapper;
import com.cao.reggie.service.DishFlavorService;
import com.cao.reggie.service.DishService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Autowired
    private DishFlavorService dishFlavorService;

    @Override
    public void saveWithFlavor(DishDto dishDto) {
        //存储菜品
        this.save(dishDto);
        //获得插入dish数据时的主键ID
        Long dishId = dishDto.getId();

        //获得口味List,并向List当中每个dishflavor写入dishID
        List<DishFlavor> flavors = dishDto.getFlavors();
        flavors = flavors.stream().map(flavor -> {
            flavor.setDishId(dishId);
            return flavor;
        }).collect(Collectors.toList());

        //保存菜品口味数据到菜品口味表dish_flavor
        dishFlavorService.saveBatch(flavors);
    }
}
