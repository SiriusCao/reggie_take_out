package com.cao.reggie.controller;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cao.reggie.common.R;
import com.cao.reggie.dto.DishDto;
import com.cao.reggie.entity.Dish;
import com.cao.reggie.entity.DishFlavor;
import com.cao.reggie.service.DishFlavorService;
import com.cao.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/dish")
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 新增菜品
     *
     * @param dishDto 菜品和其口味
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto) {
        log.info("新增菜品{}", dishDto.toString());
        dishService.saveWithFlavor(dishDto);

        //清理某个分类下面的菜品缓存数据
        String key = "dish_" + dishDto.getCategoryId() + "_1";
        redisTemplate.delete(key);

        return R.success("新增菜品成功");
    }

    @GetMapping("/page")
    public R<Page<DishDto>> page(int page, int pageSize, String name) {
        Page<DishDto> dishDtoPage = dishService.pageWithCategoryName(page, pageSize, name);
        return R.success(dishDtoPage);
    }

    @GetMapping("/{id}")
    public R<DishDto> findById(@PathVariable Long id) {
        DishDto dishDto = dishService.findByIdWithFlavors(id);
        return R.success(dishDto);
    }

    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto) {
        log.info("更新菜品信息{}", dishDto.toString());

        //清除掉旧的缓存
        Long categoryId = dishService.getById(dishDto.getId()).getCategoryId();
        String key = "dish_" + categoryId + "_1";
        redisTemplate.delete(key);

        dishService.updateWithFlavor(dishDto);

        //清理某个分类下面的菜品缓存数据
        key = "dish_" + dishDto.getCategoryId() + "_1";
        redisTemplate.delete(key);

        return R.success("更新菜品成功");
    }

    /**
     * 根据categoryId查询菜品
     *
     * @param dish
     * @return
     */
    @GetMapping("/list")
    public R<List<DishDto>> listByCategoryId(Dish dish) {
        List<DishDto> dishDtoList = null;
        String key = "dish_" + dish.getCategoryId() + "_" + dish.getStatus();
        //向redis当中查询
        dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);
        //如果redis当中存在那么直接返回
        if (dishDtoList != null) {
            return R.success(dishDtoList);
        }

        //如果不存在则执行查询
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Dish::getCategoryId, dish.getCategoryId());
        //只查询在售状态的
        queryWrapper.eq(Dish::getStatus, 1);
        queryWrapper.orderByAsc(Dish::getSort).orderByAsc(Dish::getUpdateTime);
        List<Dish> dishList = dishService.list(queryWrapper);

        dishDtoList = dishList.stream().map(item -> {
            DishDto dishDto = new DishDto();
            BeanUtils.copyProperties(item, dishDto);
            List<DishFlavor> dishFlavorList = dishFlavorService.list(
                    new LambdaQueryWrapper<DishFlavor>()
                            .eq(DishFlavor::getDishId, item.getId()));
            dishDto.setFlavors(dishFlavorList);
            return dishDto;
        }).collect(Collectors.toList());
//        将查询到的菜品数据缓存到Redis
        redisTemplate.opsForValue().set(key, dishDtoList, 60, TimeUnit.MINUTES);
        return R.success(dishDtoList);
    }

    @PostMapping("/status/{flag}")
    public R<String> status(@PathVariable int flag, @RequestParam List<Long> ids) {
        for (Long id : ids) {
            Dish dish = new Dish();
            dish.setId(id);
            dish.setStatus(flag);
            dishService.updateById(dish);

            //清除该菜品分类下的redis缓存
            Long categoryId = dishService.getById(id).getCategoryId();
            String key = "dish_" + categoryId + "_1";
            redisTemplate.delete(key);
        }
        return R.success("修改成功");
    }

    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids) {
        log.info("删除菜品{}", ids.toString());
        dishService.deleteWithFlavor(ids);
        return R.success("删除成功");
    }
}
