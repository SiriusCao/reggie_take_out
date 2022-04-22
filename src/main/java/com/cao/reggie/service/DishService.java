package com.cao.reggie.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cao.reggie.dto.DishDto;
import com.cao.reggie.entity.Dish;

public interface DishService extends IService<Dish> {
    //新增菜品，同时插入菜品对应的口味数据，需要操作两张表：dish、dish_flavor
    public void saveWithFlavor(DishDto dishDto);

    //查询菜品同时查询分类名
    public Page<DishDto> pageWithCategoryName(int page, int pageSize, String name);

    //根据菜品Id查询菜品及其口味
    public DishDto findByIdWithFlavors(Long id);
}
