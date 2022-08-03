package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.entity.Category;

/**
 * @author swh
 * @creat 2022/7/29
 */

public interface CategoryService extends IService<Category> {
    public void remove(Long id);
}
