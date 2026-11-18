package com.campus_platform.interceptor;

import com.campus_platform.entity.User;
import com.campus_platform.mapper.UserMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 升级版拦截器
 * 1. 拦截未登录
 * 2. 拦截已封号（实现“秒踢下线”）
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Autowired
    @Lazy
    private UserMapper userMapper; //注入Mapper查最新状态

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 获取Session
        HttpSession session = request.getSession();

        // 测试拦截器
        System.out.println(">>> 👮‍♂️ 正在检查请求: " + request.getRequestURI());

        Object userObj = session.getAttribute("user");

        // 未登录
        if (userObj == null) {
            // 打印日志，方便我们在后台看到是谁被拦截了
            System.out.println(">>> ⛔ 拦截器拦截请求: " + request.getRequestURI() + " (原因: 未登录)");
            response.sendRedirect("/auth/login");
            return false;
        }

        // 登录了，检查是否被封号
        User sessionUser = (User) userObj;

        // 去数据库查一下这个人的最新状态
        User dbUser = userMapper.selectById(sessionUser.getId());

        // 如果数据库里这个人没了，或者状态变成 0 (封号)
        if (dbUser == null || (dbUser.getStatus() != null && dbUser.getStatus() == 0)) {
            System.out.println(">>> 🚫 拦截已封号用户: " + sessionUser.getUsername());

            // 销毁他的 Session
            session.invalidate();

            // 踢回登录页
            response.sendRedirect("/auth/login");
            return false;
        }

        return true;

    }

}