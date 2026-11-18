package com.campus_platform.controller;

import com.campus_platform.common.Result;
import com.campus_platform.entity.Task;
import com.campus_platform.entity.User;
import com.campus_platform.service.TaskService;
import com.campus_platform.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private TaskService taskService;

    //个人中心页面
    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/auth/login";
        }

        // 1. 刷新用户信息（从数据库获取最新数据）
        User freshUser = userService.getUserById(user.getId());
        session.setAttribute("user", freshUser);
        model.addAttribute("user", freshUser);

        // 2. 获取我发布的任务
        List<Task> publishedTasks = taskService.listMyPublishedTasks(freshUser.getId());
        LocalDateTime now = LocalDateTime.now();
        for (Task task : publishedTasks) {
            // 如果是待接单状态，且当前时间超过截止时间设为过期
            boolean isExpired = task.getDeadline() != null && task.getDeadline().isBefore(now);
            task.setExpired(isExpired);
        }
        model.addAttribute("publishList", publishedTasks);

        // 3. 获取我接的任务
        List<Task> takenTasks = taskService.listMyTakenTasks(freshUser.getId());
        model.addAttribute("takeList", takenTasks);

        return "user/profile";
    }

    //更新用户信息（昵称）
    @PostMapping("/updateInfo")
    public String updateInfo(HttpSession session,
                             @RequestParam String nickname,
                             RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/auth/login";
        }

        try {
            // 调用服务层修改昵称
            userService.updateNickname(user.getId(), nickname);

            // 更新 Session 中的数据
            user.setNickname(nickname);
            session.setAttribute("user", user);

            redirectAttributes.addFlashAttribute("successMessage", "昵称修改成功！");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/user/profile";
    }

    //修改密码
    @PostMapping("/updatePwd")
    public String updatePassword(HttpSession session,
                                 @RequestParam String oldPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/auth/login";
        }

        try {
            // 验证新密码和确认密码是否一致
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("pwdError", "新密码与确认密码不一致");
                return "redirect:/user/profile";
            }

            // 调用服务层修改密码
            boolean success = userService.updatePassword(user.getId(), oldPassword, newPassword);

            if (success) {
                redirectAttributes.addFlashAttribute("pwdSuccess", "密码修改成功，请重新登录");
                // 清除session，让用户重新登录
                session.invalidate();
                return "redirect:/auth/login";
            } else {
                redirectAttributes.addFlashAttribute("pwdError", "密码修改失败");
            }
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("pwdError", e.getMessage());
        }

        return "redirect:/user/profile";
    }

   //提交实名认证
    @ResponseBody
    @PostMapping("/verify")
    public Result verify(@RequestParam String realName,
                         @RequestParam String studentNo,
                         HttpSession session) {
        // 1. 获取当前用户
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return Result.error("登录已过期，请重新登录");
        }

        try {
            // 调用 Service 层处理业务
            userService.verifyUser(user.getId(), realName, studentNo);

            // 更新 Session
            User freshUser = userService.getUserById(user.getId());
            session.setAttribute("user", freshUser);

            return Result.success("认证成功！");
        } catch (RuntimeException e) {
            // 捕获 Service 层抛出的异常并返回给前端
            return Result.error(e.getMessage());
        }
    }

    //余额充值
    @PostMapping("/recharge")
    public String recharge(HttpSession session,
                           @RequestParam("amount") Double amount,
                           RedirectAttributes redirectAttributes) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/auth/login";
        }

        try {
            userService.recharge(user.getId(), amount);

            // 更新session中的用户信息
            User freshUser = userService.getUserById(user.getId());
            session.setAttribute("user", freshUser);

            redirectAttributes.addFlashAttribute("pwdSuccess", "充值成功！");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("pwdError", e.getMessage());
        }

        return "redirect:/user/profile";
    }
}