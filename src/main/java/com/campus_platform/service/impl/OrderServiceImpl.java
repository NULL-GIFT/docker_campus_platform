package com.campus_platform.service.impl;

import com.campus_platform.common.Const;
import com.campus_platform.entity.Order;
import com.campus_platform.mapper.OrderMapper;
import com.campus_platform.mapper.ItemMapper; // 借用 B 同学的
import com.campus_platform.mapper.UserMapper; // 借用 D 同学的
import com.campus_platform.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;
import java.util.List;
import org.springframework.scheduling.annotation.Scheduled; // 引入注解

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private ItemMapper itemMapper; // 用于修改商品状态

    @Autowired
    private UserMapper userMapper; // 用于扣除用户余额

    //创建订单
    @Override
    public String createOrder(Long itemId, Long buyerId, java.math.BigDecimal amount) {
        Order order = new Order();
        String orderNo = UUID.randomUUID().toString().replace("-", ""); // 生成唯一订单号

        order.setOrderNo(orderNo);
        order.setItemId(itemId);
        order.setBuyerId(buyerId);
        order.setAmount(amount);
        order.setStatus(Const.ORDER_STATUS_UNPAID); // 初始状态为 0 (待支付)

        orderMapper.insert(order);
        return orderNo;
    }

    // 模拟支付
    @Override
    @Transactional // 保证支付过程的原子性，错一个全回滚
    public boolean payOrder(String orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null || order.getStatus() != Const.ORDER_STATUS_UNPAID) return false;

        // 校验并扣除余额（调用 D 的接口）
        int updatedBalance = userMapper.decreaseBalance(order.getBuyerId(), order.getAmount());
        if (updatedBalance == 0) throw new RuntimeException("余额不足，支付失败");

        // 修改商品状态为已售出（调用 B 的接口）
        itemMapper.updateStatus(order.getItemId(), Const.ITEM_STATUS_SOLD);

        // 更新订单状态为已支付
        orderMapper.updateStatus(orderNo, Const.ORDER_STATUS_PAID);

        return true;
    }

    // 获取个人订单列表
    @Override
    public List<Order> getMyOrders(Long buyerId) {
        return orderMapper.selectByBuyerId(buyerId);
    }

    // 取消订单
    @Override
    public boolean cancelOrder(String orderNo) {
        //大于 0：成功删除了订单 → 取消成功（true）
        //等于 0：没有找到对应的订单 → 取消失败（false）
        return orderMapper.deleteByOrderNo(orderNo) > 0;
    }

    //确认收货
    @Override
    @Transactional
    public boolean finishOrder(String orderNo) {
        // 获取订单信息
        Order order = orderMapper.selectByOrderNo(orderNo);

        // 只有“已支付”的订单才能点击“确认收货”
        if (order != null && order.getStatus() == Const.ORDER_STATUS_PAID) {
            // 将订单状态改为 2 (已完成)
            return orderMapper.updateStatus(orderNo, Const.ORDER_STATUS_FINISHED) > 0;
        }
        return false;
    }

    //根据用户id和订单状态查询订单
    @Override
    public List<Order> getOrdersByStatus(Long buyerId, Integer status) {
        return orderMapper.selectByBuyerIdAndStatus(buyerId, status);
    }

    //根据订单号查询详情
    @Override
    public Order selectByOrderNo(String orderNo) {
        return orderMapper.selectByOrderNo(orderNo);
    }

    //定时任务：每隔 1 分钟执行一次扫描
    //超过 15 分钟未支付的订单并自动取消（这里用于测试采用1分钟）
    @Scheduled(cron = "0 */1 * * * ?") // cron表达式：每分钟的第0秒执行（意思是从 0 秒开始，每过 1 分钟执行一次。）
    public void closeOrderTask() {
        // 查询所有 状态为0(待支付) 且 超过15分钟 的订单（这里用于测试采用1分钟）
        List<Order> timeoutOrders = orderMapper.selectTimeoutOrders(0, 1);

        if (timeoutOrders != null && !timeoutOrders.isEmpty()) {
            System.out.println("--- [系统自动巡检] 发现 " + timeoutOrders.size() + " 个超时订单 ---");

            for (Order order : timeoutOrders) {
                this.cancelOrder(order.getOrderNo());
                System.out.println("已自动取消超时订单：" + order.getOrderNo());
            }
        }
    }

    //根据订单号或商品名称进行模糊查询订单
    @Override
    public List<Order> searchOrders(Long buyerId, String keyword, Integer status) {
        return orderMapper.searchOrders(buyerId, keyword, status);
    }

    // 按状态筛选卖家订单
    @Override
    public List<Order> getMySoldOrders(Long sellerId, Integer status) {
        return orderMapper.selectBySellerIdAndStatus(sellerId, status);
    }

    //关键词模糊搜索我卖出的订单
    @Override
    public List<Order> searchSoldOrders(Long sellerId, String keyword, Integer status) {
        return orderMapper.searchSoldOrders(sellerId, keyword, status);
    }
}