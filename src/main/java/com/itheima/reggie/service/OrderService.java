package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.entity.Orders;

/**
 * @author swh
 * @creat 2022/8/2
 */
public interface OrderService extends IService<Orders> {

    /**
     * 用户下单
     * @param orders
     */
    void submitByUser(Orders orders);
}
