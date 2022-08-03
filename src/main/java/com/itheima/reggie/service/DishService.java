package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Dish;

import java.util.List;

/**
 * @author swh
 * @creat 2022/7/29
 */
public interface DishService extends IService<Dish> {

    //新增菜品，同时插入菜品对应的口味数据，需要操作两张表：dish   dish_flavor
    public void saveWithFlavor(DishDto dishDto);

    //根据id查询菜品信息和对应的口味信息
    public DishDto getByIdWithFlavor(Long id);

    //修改菜品，同时修改菜品对应的口味数据，需要操作两张表：dish   dish_flavor
    public void updateWithFlavor(DishDto dishDto);

    //根据传过来的id单个或者批量删除菜品
    void deleteByIdS(List<Long> ids);
}
