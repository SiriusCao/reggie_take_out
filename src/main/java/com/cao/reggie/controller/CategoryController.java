package com.cao.reggie.controller;

import com.cao.reggie.common.R;
import com.cao.reggie.entity.Category;
import com.cao.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
     * @param category 分类实体类
     * @return 状态信息
     */
    @PostMapping
    public R<String> save(@RequestBody Category category){
        log.info("新增{}",category.toString());
        categoryService.save(category);
        return R.success("新增成功");
    }
}
