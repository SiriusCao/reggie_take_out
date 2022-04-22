package com.cao.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cao.reggie.common.R;
import com.cao.reggie.entity.Category;
import com.cao.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    /**
     * 删除分类
     *
     * @param id 分类ID
     * @return
     */
    @DeleteMapping
    public R<String> delete(Long id) {
        log.info("删除分类{}", id);
        categoryService.removeAfterCheck(id);
        return R.success("");
    }

    @PutMapping
    public R<String> update(@RequestBody Category category) {
        log.info("修改{}", category.toString());
        boolean b = categoryService.updateById(category);
        if (b) {
            return R.success("");
        }
        return R.error("更新失败");
    }

    /**
     * 查询菜品分类
     *
     * @param category 将传入过来的type封装进category实体类
     * @return
     */
    @RequestMapping("/list")
    public R<List<Category>> list(Category category) {
        //构造查询条件
        LambdaQueryWrapper<Category> categoryLambdaQueryWrapper = new LambdaQueryWrapper<>();
        categoryLambdaQueryWrapper.eq(category != null && category.getType() != null, Category::getType, category.getType());
        //排序方式
        categoryLambdaQueryWrapper.orderByAsc(Category::getSort).orderByAsc(Category::getUpdateTime);
        //根据条件查询
        List<Category> categoryList = categoryService.list(categoryLambdaQueryWrapper);
        return R.success(categoryList);
    }
}
