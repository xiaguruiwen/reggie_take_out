package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Orders;
import com.itheima.reggie.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

/**
 * @author swh
 * @creat 2022/8/2
 */
@RestController
@Slf4j
@RequestMapping("/order")
public class OrderController {
    @Autowired
    private OrderService orderService;

    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String number, String beginTime,String endTime){
        log.info("page:{}",page);
        log.info("pageSize:{}",pageSize);
        log.info("number:{}",number);
        log.info("beginTime:{}",beginTime);
        log.info("endTime:{}",endTime);

        Page<Orders> pageInfo = new Page<>(page,pageSize);
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(number != null,Orders::getNumber,number)
                .gt(beginTime != null,Orders::getOrderTime,beginTime)
                .lt(endTime != null, Orders::getOrderTime,endTime);
        orderService.page(pageInfo,queryWrapper);
        return R.success(pageInfo);
    }


    /**
     * 用户下单
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        log.info("订单数据：{}",orders);
        orderService.submitByUser(orders);
        return R.success("下单成功");
    }
}
