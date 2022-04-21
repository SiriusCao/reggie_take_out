package com.cao.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cao.reggie.common.R;
import com.cao.reggie.entity.Category;
import com.cao.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 分类管理
 */
@RestController
@Slf4j
@RequestMapping("/category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 新增分类
     *
     * @param category 分类实体类
     * @return 状态信息
     */
    @PostMapping
    public R<String> save(@RequestBody Category category) {
        log.info("新增{}", category.toString());
        categoryService.save(category);
        return R.success("新增成功");
    }

    /**
     * 分页查询
     *
     * @param page     页码
     * @param pageSize 每页显示几个数据
     * @return 分页信息
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize) {
        //分页构造器
        Page<Category> pageInfo = new Page(page, pageSize);
        //条件构造器
        LambdaQueryWrapper<Category> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByAsc(Category::getSort);
        //查询
        categoryService.page(pageInfo, queryWrapper);
        //返回分页数据
        return R.success(pageInfo);
    }
}
