package com.campus_platform.common;

/**
 * 全局常量定义类
 */
public class Const {
    // 订单状态定义
    public static final int ORDER_STATUS_UNPAID = 0;  // 待支付--0
    public static final int ORDER_STATUS_PAID = 1;    // 已支付--1
    public static final int ORDER_STATUS_FINISHED = 2; // 已完成（确认收货）--2

    // 商品状态定义（对接 B 同学）
    public static final int ITEM_STATUS_ON_SALE = 0;  // 在售
    public static final int ITEM_STATUS_SOLD = 1; // 已售出

}