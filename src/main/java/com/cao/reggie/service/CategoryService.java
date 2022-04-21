package com.cao.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.cao.reggie.entity.Category;

public interface CategoryService extends IService<Category> {
    public void removeAfterCheck(Long id);
}
