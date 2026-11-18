package com.campus_platform.service;

import com.campus_platform.entity.Order;

import java.util.List;

public interface OrderService {
    //创建订单
    String createOrder(Long itemId, Long buyerId, java.math.BigDecimal amount);

    // 模拟支付（Day 3 核心：最复杂的逻辑）
    boolean payOrder(String orderNo);


    // 获取个人订单列表
    List<Order> getMyOrders(Long buyerId);

    // 取消订单
    boolean cancelOrder(String orderNo);

    //确认收货
    //传入订单号，返回true/false显示是否操作成功
    boolean finishOrder(String orderNo);

    //根据用户id和订单状态查询订单
    List<Order> getOrdersByStatus(Long buyerId, Integer status);

    //根据订单号查询详情
    Order selectByOrderNo(String orderNo);

    //根据订单号或商品名称进行模糊查询订单
    List<Order> searchOrders(Long buyerId, String keyword, Integer status);

    //查询我卖出的订单
    List<Order> getMySoldOrders(Long sellerId, Integer status);

    //模糊搜索我卖出的订单
    List<Order> searchSoldOrders(Long sellerId, String keyword, Integer status);
}
