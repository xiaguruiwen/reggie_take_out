package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author swh
 * @creat 2022/8/3
 */
@RestController
@Slf4j
@RequestMapping("/shoppingCart")
public class ShoppingCartController {
    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 添加购物车
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){

        //设置用户id，指定当前是哪个用户的购物车数据
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);

        Long dishId = shoppingCart.getDishId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,currentId);

        if (dishId != null){
            //说明添加到购物车的是菜品
            queryWrapper.eq(ShoppingCart::getDishId,dishId);
        }else {
            //说明添加到购物车的是套餐
            queryWrapper.eq(ShoppingCart::getSetmealId,shoppingCart.getSetmealId());
        }

        //查询当前菜品或者套餐是否在购物车中
        ShoppingCart cart = shoppingCartService.getOne(queryWrapper);

        if(cart != null){
            //如果已经存在，就在原来数量基础上加一
            Integer number = cart.getNumber();
            cart.setNumber(number + 1);
            shoppingCartService.updateById(cart);
        }else {
            //如果不存在，则添加到购物车，数量默认就是1
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            cart = shoppingCart;
        }
        return R.success(cart);
    }

    /**
     * 查看购物车
     * @return
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list(){
        log.info("查看购物车...");
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        queryWrapper.orderByAsc(ShoppingCart::getCreateTime);

        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);
        return R.success(list);
    }

    /**
     * 清空购物车
     * @return
     */
    @DeleteMapping("/clean")
    public R<String> clean(){
        shoppingCartService.cleanShopping();
        return R.success("清空购物车成功");
    }

    /**
     * 减少购物车菜品数量
     * @return
     */
    @Transactional
    @PostMapping("/sub")
    public R<ShoppingCart> sub(@RequestBody ShoppingCart shoppingCart){
        //设置用户id，指定当前是哪个用户的购物车数据
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);

        Long dishId = shoppingCart.getDishId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,currentId);
        if(dishId != null){
            //说明减少的是菜品数
            queryWrapper.eq(ShoppingCart::getDishId,dishId);
            ShoppingCart cart = shoppingCartService.getOne(queryWrapper);
            cart.setNumber(cart.getNumber() - 1);
            Integer number = cart.getNumber();
            if (number > 0){
                shoppingCartService.updateById(cart);
            }else if(number == 0){
                shoppingCartService.removeById(cart.getId());
            }else {
                return R.error("操作异常");
            }
            return R.success(cart);
        }
        Long setmealId = shoppingCart.getSetmealId();
        if (setmealId != null){
            //说明减少的是套餐数
            queryWrapper.eq(ShoppingCart::getSetmealId,setmealId);
            ShoppingCart cart = shoppingCartService.getOne(queryWrapper);
            cart.setNumber(cart.getNumber() - 1);
            Integer number = cart.getNumber();
            if (number > 0){
                shoppingCartService.updateById(cart);
            }else if(number == 0){
                shoppingCartService.removeById(cart.getId());
            }else {
                return R.error("操作异常");
            }
            return R.success(cart);
        }
        return R.error("操作异常");
    }
}
