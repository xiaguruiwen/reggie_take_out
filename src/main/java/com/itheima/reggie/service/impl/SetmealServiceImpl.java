package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.mapper.SetmealMapper;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author swh
 * @creat 2022/7/29
 */
@Service
@Slf4j
public class SetmealServiceImpl extends ServiceImpl<SetmealMapper, Setmeal> implements SetmealService {
    @Autowired
    private SetmealDishService setmealDishService;

    /**
     * 新增套餐，同时插入套餐对应的菜品数据，需要操作两张表：setmeal   setmeal_dish
     * @param setmealDto
     */
    @Override
    @Transactional
    public void saveWithDish(SetmealDto setmealDto) {
        this.save(setmealDto);
        Long setmealDtoId = setmealDto.getId();
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        setmealDishes = setmealDishes.stream().map((item)->{
            item.setSetmealId(setmealDtoId);
            return item;
        }).collect(Collectors.toList());
       setmealDishService.saveBatch(setmealDishes);
    }

    /**
     * 删除套餐，同时删除套餐对应的菜品数据，
     * @param ids
     */
    @Override
    @Transactional
    public void removeWithDish(List<Long> ids) {
        //查询套餐的状态，确定是否可以删除
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId,ids);
        queryWrapper.eq(Setmeal::getStatus,1);
        //如果不能删除，抛出一个业务异常
        int count = this.count(queryWrapper);
        if (count > 0){
            throw new CustomException("套餐正在售卖中，不能删除");
        }
        //如果可以删除，先删除套餐表中的数据--setmeal
        this.removeByIds(ids);
        //创建条件构造器，匹配在要删除的套餐中的菜品
        LambdaQueryWrapper<SetmealDish> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.in(SetmealDish::getSetmealId,ids);
        //删除对应关系表中的数据--setmeal_dish
        setmealDishService.remove(lambdaQueryWrapper);
    }

    /**
     * 更改套餐售卖状态
     * @param status
     * @param ids
     */
    @Override
    public void updateStatusById(Integer status, List<Long> ids) {
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId,ids);
        List<Setmeal> list = this.list(queryWrapper);
        for (Setmeal setmeal : list ){
            if(setmeal != null){
                setmeal.setStatus(status);
                this.updateById(setmeal);
            }
        }
    }

    /**
     * 套餐数据回显
     * @param id
     * @return
     */
    @Override
    public SetmealDto getDataById(Long id) {
        Setmeal setmeal = this.getById(id);
        SetmealDto setmealDto = new SetmealDto();
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(id != null,SetmealDish::getSetmealId,id);
        if(setmeal != null) {
            BeanUtils.copyProperties(setmeal,setmealDto);
            List<SetmealDish> list = setmealDishService.list(queryWrapper);
            setmealDto.setSetmealDishes(list);
            return setmealDto;
        }
        return null;
    }
}
