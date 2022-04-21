package com.cao.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cao.reggie.common.CustomException;
import com.cao.reggie.entity.Category;
import com.cao.reggie.entity.Dish;
import com.cao.reggie.entity.Setmeal;
import com.cao.reggie.mapper.CategoryMapper;
import com.cao.reggie.service.CategoryService;
import com.cao.reggie.service.DishService;
import com.cao.reggie.service.SetmealService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {

    @Autowired
    private DishService dishService;

    @Autowired
    private SetmealService setmealService;

    /**
     * 根据id删除分类，删除之前需要进行判断
     *
     * @param id 分类ID
     */
    @Override
    public void removeAfterCheck(Long id) {
        //查询当前分类是否关联了菜品，如果已经关联，抛出一个业务异常
        LambdaQueryWrapper<Dish> dishLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dishLambdaQueryWrapper.eq(Dish::getCategoryId, id);
        int dishInTheCategory = dishService.count(dishLambdaQueryWrapper);
        if (dishInTheCategory > 0) {
            throw new CustomException("当前分类下关联了菜品，不能删除");
        }

        //查询当前分类是否关联了套餐，如果已经关联，抛出一个业务异常
        LambdaQueryWrapper<Setmeal> setmealLambdaQueryWrapper = new LambdaQueryWrapper<>();
        setmealLambdaQueryWrapper.eq(Setmeal::getCategoryId, id);
        int setmealInTheCategory = setmealService.count(setmealLambdaQueryWrapper);
        if (setmealInTheCategory > 0) {
            throw new CustomException("当前分类下关联了套餐，不能删除");
        }

        //以上情况均不符合，删除该分类
        this.removeById(id);
    }
}
