package com.campus_platform.controller;

import com.campus_platform.entity.Order;
import com.campus_platform.entity.User;
import com.campus_platform.mapper.UserMapper;
import com.campus_platform.service.OrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/order")
public class OrderController {
    //私有成员变量，用于调用订单业务逻辑
    @Autowired
    private OrderService orderService;

    // 在这里注入 UserMapper，pay的时候调用 D 同学的接口
    @Autowired
    private UserMapper userMapper;

    // 创建订单并跳转确认页
    @PostMapping("/create")
    public String createOrder(@RequestParam Long itemId,
                              @RequestParam java.math.BigDecimal amount,
                              HttpSession session) {
        // 这个 user 对象，是在用户登录时就已经保存到 session 中的，现在只是取出来用
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/auth/login"; // 未登录拦截
        }

        // --- 防重复下单逻辑 ---
        //查询该用户针对该商品是否已经有一个“待支付”的订单
        //确保了数据库中该用户对该商品的“待支付”记录具有唯一性

        //先获取该用户的所有订单
        List<Order> myOrders = orderService.getMyOrders(user.getId());
        for (Order existingOrder : myOrders) {
            // 如果存在：同一个商品 且 状态为 0 (待支付)
            if (existingOrder.getItemId().equals(itemId) && existingOrder.getStatus() == 0) {
                // 检查这个旧订单是否已经超时 (假设超时时间是 1 分钟)
                // 答辩演示时设置为 10 秒
                long expireTime = existingOrder.getCreateTime().getTime() + (15 * 1000L);

                //这个判断条件是用于修复bug：因为前后端的运行有延迟。
                if (System.currentTimeMillis() > expireTime) {
                    // 如果已超时，手动取消旧订单
                    orderService.cancelOrder(existingOrder.getOrderNo());
                    continue; // 跳过当前循环，去下面创建新订单
                }

                // 直接跳转到该订单的确认页，不产生新记录
                return "redirect:/order/confirm?orderNo=" + existingOrder.getOrderNo();
            }
        }

        //如果没有旧订单，再正常创建
        String orderNo = orderService.createOrder(itemId, user.getId(), amount);

        // 创建完订单后，跳转到确认支付页面
        return "redirect:/order/confirm?orderNo=" + orderNo;
    }

    // 支付确认页展示
    @GetMapping("/confirm")
    public String confirmPage(@RequestParam String orderNo,
                              Model model) {
        // 查询出完整的订单信息传给前端，因为倒计时需要 createTime
        Order order = orderService.selectByOrderNo(orderNo);
        // 建议加上判空，防止订单被删了用户强行访问报错
        if (order == null) {
            return "redirect:/order/my_orders?timeout=1";
        }

        // ==================== 方案二：展示页实时拦截 ====================
        if (order.getStatus() == 0) {   // 只有处于待支付（0）状态的订单才需要校验时间
            // 计算订单的绝对失效时刻 (订单创建时间毫秒数 + 10秒)
            long expireTime = order.getCreateTime().getTime() + (15 * 1000L);

            // 如果当前服务器系统时间已经大于失效时刻，说明已经超时
            if (System.currentTimeMillis() > expireTime) {
                // 立即调用取消接口，从数据库中抹去这条僵尸记录
                orderService.cancelOrder(orderNo);
                return "redirect:/order/my_orders?timeout=1";
            }
        }
        // ====================================================================

        // 如果未超时，则正常执行原有的视图绑定
        model.addAttribute("orderNo", orderNo);
        model.addAttribute("order", order);

        return "order/confirm"; // 返回 confirm.html
    }

    // 执行模拟支付
    @GetMapping("/pay")
    public String pay(@RequestParam String orderNo,
                      HttpSession session) {

        // ==================== 方案二：支付核心前置拦截 ====================
        Order order = orderService.selectByOrderNo(orderNo);
        if (order != null && order.getStatus() == 0) {
            long expireTime = order.getCreateTime().getTime() + (15 * 1000L); // 10 秒超时，方便答辩演示

            // 临门一脚时如果发现超时，立刻进行拦截并斩杀订单，坚决不执行后续扣款
            if (System.currentTimeMillis() > expireTime) {
                orderService.cancelOrder(orderNo); // 后端主动清理数据库
                return "redirect:/order/my_orders?payTimeout=1";
            }
        }
        // ====================================================================

        try {
            // 只有通过了上面时间防线的有效订单，才会进入到 payOrder 内部去扣除买家余额、改商品状态
            boolean success = orderService.payOrder(orderNo);

            if (success) {
                // 更新 session 中的余额
                User currentUser = (User) session.getAttribute("user");
                User updatedUser = userMapper.selectById(currentUser.getId());
                session.setAttribute("user", updatedUser);

                return "redirect:/order/success?paid=1";
            }

            // payOrder 返回 false，说明订单不存在、订单状态异常或已不是待支付状态
            return "redirect:/order/confirm?orderNo=" + orderNo + "&payError=invalid";

        } catch (RuntimeException e) {
            // 容器环境下不再依赖 addFlashAttribute，改为 URL 参数提示，避免重定向后提示丢失
            return "redirect:/order/confirm?orderNo=" + orderNo + "&payError=balance";
        }
    }

    // 支付成功提示页
    @GetMapping("/success")
    public String successPage() {
        return "order/success";
    }

    // 取消订单
    @GetMapping("/cancel")
    public String cancel(@RequestParam String orderNo) {
        orderService.cancelOrder(orderNo);
        return "redirect:/market/list"; // 取消后回市场
    }

    //确认收货
    @GetMapping("/finish")
    public String finish(@RequestParam String orderNo) {
        boolean success = orderService.finishOrder(orderNo);
        // 操作完成后重定向回“我的订单”页面
        return "redirect:/order/my_orders";
    }

    //进入订单中心页面时，按订单状态筛选
    @GetMapping("/my_orders")
    public String myOrders(@RequestParam(value = "status", required = false) Integer status,
                           @RequestParam(value = "type", defaultValue = "buy") String type,
                           HttpSession session, //从session中取出当前用户
                           Model model) {  //视图模型，用于在Controller和视图（HTML/JSP）之间传递数据
        // 获取登录用户信息
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/auth/login"; // 没登录先去登录
        }
        List<Order> orderList;

        // 根据 type 判断是查询“买入”还是“卖出”
        if ("sell".equals(type)) {
            // 查询我卖出的
            orderList = orderService.getMySoldOrders(user.getId(), status);
        } else {
            // 查询我买到的（默认）
            orderList = orderService.getOrdersByStatus(user.getId(), status);
        }

        // 将数据放入模型
        model.addAttribute("orders", orderList);
        model.addAttribute("currentStatus", status);
        model.addAttribute("currentType", type); // 将当前类型传给前端，用于按钮高亮和逻辑判断

        return "order/my_orders";
    }


    //查看订单详情，处理 AJAX 请求的方法
    @GetMapping("/api/detail/{orderNo}")
    @ResponseBody  //不返回视图页面，而是返回 JSON/XML 等数据格式
    public Map<String, Object> getOrderDetail(@PathVariable String orderNo, HttpSession session) {
        //返回类型是Map<String, Object>键值对集合，便于构建 JSON 响应
        Map<String, Object> result = new HashMap<>();

        Order order = orderService.selectByOrderNo(orderNo);
        User user = (User) session.getAttribute("user");

        if (order != null && user != null) {
            //只有订单存在和用户已登录、会话中有用户信息两个条件同时满足才返回订单详情
            result.put("code", 200);
            result.put("orderNo", order.getOrderNo());
            result.put("amount", order.getAmount());
            result.put("createTime", new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(order.getCreateTime()));
            result.put("buyerName", user.getNickname());  //将买家昵称添加到响应结果
            result.put("status", order.getStatus());
            result.put("itemName", "校园二手商品 (ID: " + order.getItemId() + ")");
        } else {
            result.put("code", 500);
            result.put("msg", "未查询到订单信息");
        }
        return result;
    }

    //在订单页面内搜索时，根据订单号或商品名称进行模糊查询订单（区分是买家还是卖家）
    @GetMapping("/search")
    public String searchOrder(@RequestParam("keyword") String keyword,
                              @RequestParam(value = "type", defaultValue = "buy") String type, // 接收 type 参数
                              @RequestParam(value = "status", required = false) Integer status, // 接收 status 参数
                              HttpSession session,
                              Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) return "redirect:/auth/login";

        List<Order> searchResult;
        String trimmedKeyword = (keyword != null) ? keyword.trim() : "";

        if ("sell".equals(type)) {
            // 如果是在“我卖出的”页面点击搜索
            searchResult = orderService.searchSoldOrders(user.getId(), trimmedKeyword, status);
        } else {
            if (trimmedKeyword.isEmpty()) {
                // 如果没有输入关键字，就去查该状态下的所有订单，直接复用原来这个方法即可
                searchResult = orderService.getOrdersByStatus(user.getId(), status);
            } else {
                searchResult = orderService.searchOrders(user.getId(), trimmedKeyword, status);
            }
        }
        model.addAttribute("orders", searchResult);
        model.addAttribute("keyword", keyword);
        model.addAttribute("currentType", type); // 必须传回前端，否则按钮高亮会消失，导致切回买家视图
        model.addAttribute("currentStatus", status);

        return "order/my_orders";
    }
}