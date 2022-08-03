package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;

import java.util.List;

/**
 * @author swh
 * @creat 2022/7/29
 */
public interface SetmealService extends IService<Setmeal> {
    /**
     * 新增套餐，同时插入套餐对应的菜品数据，需要操作两张表：setmeal   setmeal_dish
     * @param setmealDto
     */
    public void saveWithDish(SetmealDto setmealDto);

    /**
     * 删除套餐，同时删除套餐对应的菜品数据，
     * @param ids
     */
    void removeWithDish(List<Long> ids);

    /**
     * 更改套餐售卖状态
     * @param status
     * @param ids
     */
    void updateStatusById(Integer status,List<Long> ids);

    /**
     * 回显套餐数据
     * @param id
     */
    SetmealDto getDataById(Long id);
}
