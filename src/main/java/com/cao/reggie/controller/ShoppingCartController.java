package com.cao.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cao.reggie.common.BaseContext;
import com.cao.reggie.common.R;
import com.cao.reggie.entity.ShoppingCart;
import com.cao.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/shoppingCart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    @GetMapping("/list")
    public R<List<ShoppingCart>> list() {
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
        wrapper.orderByAsc(ShoppingCart::getCreateTime);
        List<ShoppingCart> shoppingCartList = shoppingCartService.list(wrapper);
        return R.success(shoppingCartList);
    }

    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart) {
        //获取当前用户的id
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId, currentId);

        //先查询购物车是否有重复的，有则number+1，否则插入新纪录
        Long dishId = shoppingCart.getDishId();
        if (dishId != null) {
            wrapper.eq(ShoppingCart::getDishId, dishId);
            wrapper.eq(shoppingCart.getDishFlavor() != null, ShoppingCart::getDishFlavor, shoppingCart.getDishFlavor());
        } else {
            wrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }
        ShoppingCart one = shoppingCartService.getOne(wrapper);
        if (one != null) {
            Integer number = one.getNumber();
            one.setNumber(number + 1);
            shoppingCartService.updateById(one);
        } else {
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            one = shoppingCart;
        }
        return R.success(one);
    }

    @DeleteMapping("/clean")
    public R<String> clean() {
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
        boolean remove = shoppingCartService.remove(wrapper);
        if (remove) {
            return R.success("删除成功");
        }
        return R.error("删除失败");
    }


    /**
     * 购物车中的物品数量—1
     *
     * @param shoppingCart 当前物品的ID
     * @return 返回被操作物品当前的实体类
     */
    @PostMapping("/sub")
    public R<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart) {
        //获取当前用户ID
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);

        //将用户ID加入条件过滤器
        LambdaQueryWrapper<ShoppingCart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShoppingCart::getUserId, currentId);

        //判断是套餐还是蔡品,并分别赋予条件过滤器对应的ID
        Long dishId = shoppingCart.getDishId();
        if (dishId != null) {
            wrapper.eq(ShoppingCart::getDishId, dishId);
        } else {
            wrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }

        //获取对应的购物车记录
        ShoppingCart one = shoppingCartService.getOne(wrapper);

        if (one != null) {
            Integer number = one.getNumber();
            //如果购物车当中该条商品有不止一个，就将number减1然后更新
            if (number > 1) {
                one.setNumber(number - 1);
                shoppingCartService.updateById(one);
            } else if (number == 1) {
                //如果购物车当中改价调记录为0，则清除该记录并将one实体类实例当中的number置为0
                shoppingCartService.remove(wrapper);
                one.setNumber(0);
            }
        }
        return R.success(one);
    }
}
