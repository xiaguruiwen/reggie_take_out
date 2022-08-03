package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.Setmeal;
import com.itheima.reggie.entity.SetmealDish;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.SetmealDishService;
import com.itheima.reggie.service.SetmealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 套餐管理
 * @author swh
 * @creat 2022/7/31
 */
@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetmealController {
    @Autowired
    private SetmealService setmealService;
    @Autowired
    private SetmealDishService setmealDishService;
    @Autowired
    private CategoryService categoryService;

    /**
     * 添加套餐
     * @param setmealDto
     * @return
     */
    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto){
        setmealService.saveWithDish(setmealDto);
        return R.success("成功添加套餐");
    }


    /**
     * 套餐分页展示查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page,int pageSize,String name){
        //分页构造器
        Page<Setmeal> setmealPage = new Page<>(page,pageSize);
        Page<SetmealDto> dtoPage = new Page<>();

        //条件构造器
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper();
        //添加条件
        queryWrapper.like(name != null,Setmeal::getName,name);
        //添加排序条件，根据sort进行排序
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        //进行分页查询
        setmealService.page(setmealPage,queryWrapper);

        //对象拷贝
        BeanUtils.copyProperties(setmealPage,dtoPage,"records");

        List<Setmeal> records = setmealPage.getRecords();

        List<SetmealDto> list = records.stream().map((item)->{
            SetmealDto setmealDto = new SetmealDto();
            //对象拷贝
            BeanUtils.copyProperties(item,setmealDto);
            //获取每个套餐的分类id
            Long categoryId = item.getCategoryId();
            //根据分类id查询分类对象
            Category category = categoryService.getById(categoryId);
            if(category != null){
                //分类名称
                String categoryName = category.getName();
                setmealDto.setCategoryName(categoryName);
            }
            return setmealDto;
        }).collect(Collectors.toList());

        dtoPage.setRecords(list);
        return R.success(dtoPage);
    }

    /**
     * 删除套餐
     * @param ids
     * @return
     */
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){
        log.info("ids:{}",ids);
        setmealService.removeWithDish(ids);
        return R.success("成功删除套餐");
    }

    /**
     * 更改套餐售卖状态
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    public R<String> updateStatus(@PathVariable Integer status,@RequestParam List<Long> ids){
        setmealService.updateStatusById(status,ids);
        return R.success("成功修改售卖状态");
    }

    /**
     * 套餐数据回显
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public R<SetmealDto> getData(@PathVariable Long id){
        log.info("id:{}",id);
        SetmealDto setmealDto = setmealService.getDataById(id);
        return R.success(setmealDto);
    }

    /**
     * 修改套餐内容并保存
     * @param setmealDto
     * @return
     */
    @PutMapping
    public R<String> update(@RequestBody SetmealDto setmealDto){
        if (setmealDto == null){
            return R.error("请求异常");
        }
        if (setmealDto.getSetmealDishes() == null){
            return R.error("套餐没有添加菜品，请先添加菜品");
        }
        List<SetmealDish> setmealDishes = setmealDto.getSetmealDishes();
        Long setmealId = setmealDto.getId();
        LambdaQueryWrapper<SetmealDish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SetmealDish::getSetmealId,setmealId);
        setmealDishService.remove(queryWrapper);

        for(SetmealDish setmealDish : setmealDishes){
            setmealDish.setSetmealId(setmealId);
        }
        setmealDishService.saveBatch(setmealDishes);
        setmealService.updateById(setmealDto);
        return R.success("套餐修改成功");
    }

    /**
     * 移动端套餐数据显示
     * @param setmeal
     * @return
     */
    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal){
        //条件构造器
        LambdaQueryWrapper<Setmeal> queryWrapper = new LambdaQueryWrapper();
        //添加条件
        queryWrapper.eq(setmeal.getCategoryId() != null,Setmeal::getCategoryId,setmeal.getCategoryId());
        //添加条件，查询状态必须为 1，启售状态
        queryWrapper.eq(Setmeal::getStatus,1);
        //添加排序条件
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        //执行查询
        List<Setmeal> list = setmealService.list(queryWrapper);
        return R.success(list);
    }
}
