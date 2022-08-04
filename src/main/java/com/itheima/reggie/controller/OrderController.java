package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.OrdersDto;
import com.itheima.reggie.entity.OrderDetail;
import com.itheima.reggie.entity.Orders;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.service.OrderDetailService;
import com.itheima.reggie.service.OrderService;
import com.itheima.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    @Autowired
    private OrderDetailService orderDetailService;
    @Autowired
    private ShoppingCartService shoppingCartService;

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

    public List<OrderDetail> getPrderDetailByOrderId(Long orderId){
        LambdaQueryWrapper<OrderDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderDetail::getOrderId,orderId);
        List<OrderDetail> orderDetails = orderDetailService.list(queryWrapper);
        return orderDetails;
    }

    /**
     * 用户查看自己订单
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/userPage")
    public R<Page> page(int page,int pageSize){
        //构造分页对象
        Page<Orders> pageInfo = new Page<>(page,pageSize);
        Page<OrdersDto> dtoPage = new Page<>(page,pageSize);
        //构造查询条件
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getUserId, BaseContext.getCurrentId());
        queryWrapper.orderByDesc(Orders::getOrderTime);
        orderService.page(pageInfo,queryWrapper);
        //将pageInfo的数据除了records的都传给dtoPage
        BeanUtils.copyProperties(pageInfo,dtoPage,"records");

        List<Orders> records = pageInfo.getRecords();
        List<OrdersDto> ordersDtoList = records.stream().map((item)->{
            OrdersDto ordersDto = new OrdersDto();
            Long orderId = item.getId();
            List<OrderDetail> orderDetails = this.getPrderDetailByOrderId(orderId);
            BeanUtils.copyProperties(item,ordersDto);
            ordersDto.setOrderDetails(orderDetails);
            return ordersDto;
        }).collect(Collectors.toList());

        dtoPage.setRecords(ordersDtoList);
        return R.success(dtoPage);
    }

    /**
     * 修改订单状态
     * @param map
     * @return
     */
    @PutMapping
    public R<String> OrderStatusChange(@RequestBody Map<String,String> map){
        String id = map.get("id");
        Long orderId = Long.parseLong(id);
        Integer status = Integer.parseInt(map.get("status"));

        if (orderId == null || status == null){
            return R.error("信息非法");
        }
        Orders orders = orderService.getById(orderId);
        orders.setStatus(status);
        orderService.updateById(orders);
        return R.success("订单状态修改成功");
    }

    /**
     * 用户再来一单功能实现
     * @param map
     * @return
     */
    @PostMapping("/again")
    public R<String> again(@RequestBody Map<String,String> map){
        String orderId = map.get("id");
        Long id = Long.parseLong(orderId);
        LambdaQueryWrapper<OrderDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderDetail::getOrderId,id);

        List<OrderDetail> orderDetailList = orderDetailService.list(queryWrapper);
        //清空购物车
        shoppingCartService.cleanShopping();

        Long userId = BaseContext.getCurrentId();
        List<ShoppingCart> shoppingCartList = orderDetailList.stream().map((item)->{
            ShoppingCart shoppingCart = new ShoppingCart();
            shoppingCart.setUserId(userId);
            shoppingCart.setImage(item.getImage());
            Long dishId = item.getDishId();
            Long setmealId = item.getSetmealId();
            if (dishId != null){
                //说明是菜品
                shoppingCart.setDishId(dishId);
            }else {
                //说明是套餐
                shoppingCart.setSetmealId(setmealId);
            }
            shoppingCart.setName(item.getName());
            shoppingCart.setDishFlavor(item.getDishFlavor());
            shoppingCart.setNumber(item.getNumber());
            shoppingCart.setAmount(item.getAmount());
            shoppingCart.setCreateTime(LocalDateTime.now());
            return shoppingCart;
        }).collect(Collectors.toList());
        shoppingCartService.saveBatch(shoppingCartList);
        return R.success("操作成功");
    }
}
