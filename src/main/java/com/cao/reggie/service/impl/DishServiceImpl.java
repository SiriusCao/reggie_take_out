package com.cao.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cao.reggie.common.R;
import com.cao.reggie.dto.DishDto;
import com.cao.reggie.entity.Category;
import com.cao.reggie.entity.Dish;
import com.cao.reggie.entity.DishFlavor;
import com.cao.reggie.mapper.DishMapper;
import com.cao.reggie.service.CategoryService;
import com.cao.reggie.service.DishFlavorService;
import com.cao.reggie.service.DishService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {
    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

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

    @Override
    public Page<DishDto> pageWithCategoryName(int page, int pageSize, String name) {
        //构造分页对象
        Page<Dish> dishPage=new Page<>(page,pageSize);

        //构造条件查询对象
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper=new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.eq(name!=null,Dish::getName,name);
        dishLambdaQueryWrapper.orderByAsc(Dish::getUpdateTime);

        //查询
        this.page(dishPage,dishLambdaQueryWrapper);

        //构造DTO分页对象
        Page<DishDto> dishDtoPage=new Page<>();
        //将已经查询出来的dish分页对象复制到DTO分页对象,除了分页数据之外
        BeanUtils.copyProperties(dishPage,dishDtoPage,"records");

        //首先获取dish分页数据
        List<Dish> records = dishPage.getRecords();
        //将dish分页数据复制到dishDTO分页数据，然后查询出对应的categoryName，并赋值
        List<DishDto> dishDtoRecords = records.stream().map(dish -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(dish, dishDto);
            Category category = categoryService.getById(dish.getCategoryId());
            if (category != null) {
                dishDto.setCategoryName(category.getName());
            }
            return dishDto;
        }).collect(Collectors.toList());

        //将包含有categoryName的page对象返回
        dishDtoPage.setRecords(dishDtoRecords);
        return dishDtoPage;
    }
}
