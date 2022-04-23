package com.cao.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cao.reggie.common.CustomException;
import com.cao.reggie.dto.SetmealDto;
import com.cao.reggie.entity.Category;
import com.cao.reggie.entity.Setmeal;
import com.cao.reggie.entity.SetmealDish;
import com.cao.reggie.mapper.SetmealMapper;
import com.cao.reggie.service.CategoryService;
import com.cao.reggie.service.SetmealDishService;
import com.cao.reggie.service.SetmealService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {
    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private CategoryService categoryService;

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

    @Override
    public Page<SetmealDto> pageWithCategoryName(int page, int pageSize, String name) {
        Page<Setmeal> setmealPage=new Page<>(page,pageSize);

        LambdaQueryWrapper<Setmeal> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.like(name!=null,Setmeal::getName,name);
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);

        this.page(setmealPage,queryWrapper);

        Page<SetmealDto> setmealDtoPage=new Page<>();

        BeanUtils.copyProperties(setmealPage,setmealDtoPage,"records");

        List<Setmeal> setmealList = setmealPage.getRecords();
        List<SetmealDto> setmealDtoList = setmealList.stream().map(item -> {
            SetmealDto setmealDto = new SetmealDto();
            BeanUtils.copyProperties(item, setmealDto);
            Category category = categoryService.getById(item.getCategoryId());
            if (category != null) {
                setmealDto.setCategoryName(category.getName());
            }
            return setmealDto;
        }).collect(Collectors.toList());

        setmealDtoPage.setRecords(setmealDtoList);
        return setmealDtoPage;
    }

    @Override
    public void deleteWithDish(List<Long> ids) {
        //处于在售状态的套餐不能删除
        LambdaQueryWrapper<Setmeal> wrapper=new LambdaQueryWrapper<>();
        wrapper.in(Setmeal::getId,ids);
        wrapper.eq(Setmeal::getStatus,1);
        int count = this.count(wrapper);
        if (count>0){
            throw new CustomException("套菜正在售卖中，不能删除");
        }

        //根据IDS批量删除
        this.removeByIds(ids);

        //等价于delete from setmeal_dish where setmeal_id in(1,2,3)
        LambdaQueryWrapper<SetmealDish> setmealDishWrapper=new LambdaQueryWrapper<>();
        setmealDishWrapper.in(SetmealDish::getSetmealId,ids);
        setmealDishService.remove(setmealDishWrapper);
    }

    @Override
    public SetmealDto findByIdWithDish(Long id) {
        Setmeal setmeal = this.getById(id);
        SetmealDto setmealDto=new SetmealDto();
        BeanUtils.copyProperties(setmeal,setmealDto);

        LambdaQueryWrapper<SetmealDish> wrapper=new LambdaQueryWrapper<>();
        wrapper.eq(SetmealDish::getSetmealId,id);
        List<SetmealDish> list = setmealDishService.list(wrapper);
        setmealDto.setSetmealDishes(list);
        return setmealDto;
    }
}
