package com.campus_platform.mapper;

import com.campus_platform.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderMapper {
    //插入新订单
    int insert(Order order);

    //根据订单号查询
    Order selectByOrderNo(@Param("orderNo") String orderNo);

    //更新订单状态
    int updateStatus(@Param("orderNo") String orderNo, @Param("status") Integer status);

    //查询某个买家的所有订单
    List<Order> selectByBuyerId(@Param("buyerId") Long buyerId);

    // 取消订单（即删除方法）
    // 后续在 Service 层判断：只有 status = 0 (待支付) 时才允许调用
    int deleteByOrderNo(@Param("orderNo") String orderNo);

    //根据用户id和订单状态查询订单（买家视角）
    List<Order> selectByBuyerIdAndStatus(@Param("buyerId") Long buyerId, @Param("status") Integer status);

    //查询所有状态为待支付0且创建时间早于15分钟的订单
    List<Order> selectTimeoutOrders(@Param("status") Integer status, @Param("minutes") Integer minutes);


    //根据订单号或商品名称进行模糊查询订单
    List<Order> searchOrders(@Param("buyerId") Long buyerId, @Param("keyword") String keyword, @Param("status") Integer status);
    //根据卖家ID和状态查询订单（卖家视角）
    List<Order> selectBySellerIdAndStatus(@Param("sellerId") Long sellerId, @Param("status") Integer status);

    //模糊搜索我卖出的订单
    List<Order> searchSoldOrders(@Param("sellerId") Long sellerId, @Param("keyword") String keyword, @Param("status") Integer status);
}

