package com.campus_platform.interceptor;

import com.campus_platform.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 管理员专属拦截器
 * 守住 /admin/** 的大门
 */
@Component
public class AdminInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        // 查看是否登录
        if (user == null) {
            response.sendRedirect("/auth/login");
            return false;
        }

        // 查验身份
        if (!"ADMIN".equals(user.getRole())) {
            System.out.println(">>>普通用户 " + user.getNickname() + " 无法进入: " + request.getRequestURI());

            // 权限不足，踢回首页
            response.sendRedirect("/");
            return false;
        }

        return true;
    }
}