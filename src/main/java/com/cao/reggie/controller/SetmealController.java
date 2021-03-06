package com.cao.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cao.reggie.common.R;
import com.cao.reggie.dto.SetmealDto;
import com.cao.reggie.entity.Dish;
import com.cao.reggie.entity.Setmeal;
import com.cao.reggie.entity.SetmealDish;
import com.cao.reggie.service.DishService;
import com.cao.reggie.service.SetmealDishService;
import com.cao.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
@RequestMapping("/setmeal")
public class SetmealController {

    @Autowired
    private SetmealService setmealService;

    @Autowired
    private SetmealDishService setmealDishService;

    @Autowired
    private DishService dishService;

    /**
     * 新增套餐
     *
     * @param setmealDto
     * @return
     */
    @PostMapping
    @CacheEvict(value = "SetmealCache",allEntries = true)
    public R<String> save(@RequestBody SetmealDto setmealDto) {
        log.info("新增套餐{}", setmealDto);
        setmealService.saveWithDish(setmealDto);
        return R.success("新增套餐成功");
    }

    @GetMapping("/page")
    public R<Page<SetmealDto>> page(int page, int pageSize, String name) {
        Page<SetmealDto> pageInfo = setmealService.pageWithCategoryName(page, pageSize, name);
        return R.success(pageInfo);
    }

    /**
     * 修改停售/起售状态
     *
     * @param flag 0：停售，1：起售
     * @param ids  被操作的id
     * @return 操作结果
     */
    @PostMapping("/status/{flag}")
    @CacheEvict(value = "SetmealCache",allEntries = true)
    public R<String> status(@PathVariable int flag, @RequestParam List<Long> ids) {
        for (Long id : ids) {
            Setmeal setmeal = new Setmeal();
            setmeal.setId(id);
            setmeal.setStatus(flag);
            setmealService.updateById(setmeal);
        }
        return R.success("修改成功~");
    }

    /**
     * 删除套餐
     *
     * @param ids 待删除的id
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids) {
        log.info("删除{}", ids.toString());
        setmealService.deleteWithDish(ids);
        return R.success("删除成功~");
    }

    @GetMapping("/{id}")
    public R<SetmealDto> findById(@PathVariable Long id) {
        SetmealDto setmealDto = setmealService.findByIdWithDish(id);
        return R.success(setmealDto);
    }

    @PutMapping
    @CacheEvict(value = "SetmealCache",allEntries = true)
    public R<String> update(@RequestBody SetmealDto setmealDto) {
        log.info("修改套餐{}", setmealDto.toString());
        setmealService.updateWithDish(setmealDto);
        return R.success("修改成功");
    }

    @GetMapping("/list")
    @Cacheable(value = "SetmealCache", key = "#setmeal.categoryId + '_' + #setmeal.status")
    public R<List<Setmeal>> list(Setmeal setmeal) {
        LambdaQueryWrapper<Setmeal> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Setmeal::getCategoryId, setmeal.getCategoryId())
                .eq(Setmeal::getStatus, 1);
        List<Setmeal> setmealList = setmealService.list(wrapper);
        return R.success(setmealList);
    }

    @GetMapping("/dish/{setmealId}")
    @Cacheable(value = "SetmealCache" , key = "#setmealId")
    public R<List<Dish>> dish(@PathVariable Long setmealId) {
        List<SetmealDish> setmealDishList = setmealDishService.list(
                new LambdaQueryWrapper<SetmealDish>()
                        .eq(SetmealDish::getSetmealId, setmealId));
        List<Dish> dishList = setmealDishList.stream().map(item -> {
            return dishService.getById(item.getDishId());
        }).collect(Collectors.toList());
        return R.success(dishList);
    }


}
