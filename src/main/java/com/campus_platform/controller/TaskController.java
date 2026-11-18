package com.campus_platform.controller;

import com.campus_platform.entity.Task;
import com.campus_platform.entity.User; // 1. 记得导入 User 类
import com.campus_platform.service.TaskService;
import com.campus_platform.service.UserService;
import com.github.pagehelper.PageInfo;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import com.google.common.util.concurrent.RateLimiter;
import org.springframework.web.bind.annotation.ResponseBody;
@Controller
@RequestMapping("/task")
public class TaskController {

    // 【高并发治理核心】：每秒只发放 2 个令牌 (意味着QPS最大为2，超出直接拦截)
    private static final RateLimiter rateLimiter = RateLimiter.create(0.5);
    @Autowired
    private TaskService taskService;
    @Autowired
    private UserService userService;

    /**
     * 任务大厅, 查看可接单任务 (带分页)
     * URL: /task/list?pageNum=1
     */
    // TaskController.java
    @GetMapping("/list")
    public String list(Model model,
                       @RequestParam(defaultValue = "1") int pageNum,
                       @RequestParam(defaultValue = "all") String type,
                       HttpSession session) {

        User user = (User) session.getAttribute("user");
        PageInfo<Task> pageInfo;

        if ("mine".equals(type) && user != null) {
            // 我的发布：调用新方法
            pageInfo = taskService.listMyPublishedTasks(pageNum, 9, user.getId());
        } else {
            // 大厅任务：原有逻辑
            pageInfo = taskService.listAvailableTasks(pageNum, 9);
        }

        // 统一处理过期标记
        LocalDateTime now = LocalDateTime.now();
        for (Task task : pageInfo.getList()) {
            task.setExpired(task.getDeadline() != null && task.getDeadline().isBefore(now));
        }

        model.addAttribute("pageInfo", pageInfo);
        model.addAttribute("currentType", type);   // 用于前端高亮tab
        return "task/list";
    }
    /**
     * 跳转发布任务页面
     * URL: /task/publish
     */
    @GetMapping("/publish")
    public String publishPage() {
        return "task/publish";
    }
    /**
     * 提交发布任务
     * 主要负责接收表单数据和维护会话状态
     * URL: /task/publish
     */
    @PostMapping("/publish")
    public String publish(Task task, HttpSession session, RedirectAttributes ra){
        User user = (User) session.getAttribute("user");
        if(user == null){
            return "redirect:/auth/login";
        }

        try {
            // 发布任务 (此时业务层已经把数据库里的钱扣了)
            taskService.publishTask(task, user.getId());

            // 【新增这两行：从数据库查出最新的自己，并塞回 Session 覆盖旧数据】
            User freshUser = userService.getUserById(user.getId());
            session.setAttribute("user", freshUser);

        } catch (RuntimeException e) {
            // 捕获异常（比如余额不足）
            ra.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/task/publish";
        }

        return "redirect:/task/list";
    }

    /**
     * 抢单
     * URL: /task/take?taskId=xxx
     */
    @PostMapping("/take")
    public String take(@RequestParam("taskId") Long taskId,
                       HttpSession session,RedirectAttributes ra) {

        User user = (User) session.getAttribute("user");

        if (user == null) {
            return "redirect:/auth/login";
        }

        try {
            // 尝试抢单
            taskService.takeTask(taskId, user);
        } catch (RuntimeException e) {
            // 捕获异常（比如未实名、不能接自己单）
            String msg = e.getMessage();
            ra.addFlashAttribute("errorMsg", msg);
            if (msg.contains("未实名")) {
                ra.addFlashAttribute("needVerify", true);
            }
            return "redirect:/task/list";
        }

        return "redirect:/task/list";
    }

    @GetMapping("/detail")
    public String detail(@RequestParam("taskId") Long taskId,
                         Model model) {
        Task task = taskService.getTaskDetail(taskId);
        boolean expired = task.getDeadline() != null
                && task.getDeadline().isBefore(LocalDateTime.now());
        task.setExpired(expired);
        model.addAttribute("task", task);
        return "task/detail";
    }
    @PostMapping("/finish")
    public String finish(@RequestParam Long taskId,
                         HttpSession session) {
        User user = (User) session.getAttribute("user");
        taskService.finishTask(taskId, user.getId());
        return "redirect:/task/detail?taskId=" + taskId;
    }

    @PostMapping("/confirm")
    public String confirm(@RequestParam Long taskId,
                          HttpSession session) {
        User user = (User) session.getAttribute("user");
        taskService.confirmTask(taskId, user.getId());
        return "redirect:/task/detail?taskId=" + taskId;
    }

    // 跳转编辑页面
    @GetMapping("/edit")
    public String editPage(@RequestParam Long taskId, Model model, HttpSession session) {
        User user = (User) session.getAttribute("user");
        Task task = taskService.getTaskDetail(taskId);

        if (task == null || !user.getId().equals(task.getPublisherId()) || task.getStatus() != 0) {
            return "redirect:/task/list";
        }
        model.addAttribute("task", task);
        return "task/edit";
    }

    // 提交编辑
    @PostMapping("/update")
    public String update(Task task, HttpSession session) {
        User user = (User) session.getAttribute("user");
        taskService.updateTask(task, user.getId());
        return "redirect:/task/detail?taskId=" + task.getId();
    }



    @PostMapping("/delete")
    public String delete(@RequestParam Long taskId, HttpSession session) {
        User user = (User) session.getAttribute("user");

        try {
            // 调用普通用户的删除逻辑（必须校验身份 + 退款）
            taskService.deleteTask(taskId, user.getId());
            User freshUser = userService.getUserById(user.getId());
            session.setAttribute("user", freshUser);
        } catch (Exception e) {
            // 记录错误或返回错误提示
        }

        return "redirect:/task/list";
    }

    @PostMapping("/republish")
    public String republish(@RequestParam Long taskId, HttpSession session, RedirectAttributes ra) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return "redirect:/auth/login";
        }

        try {
            // 调用业务层的“退旧扣新”逻辑
            Task newTask = taskService.republishTask(taskId, user.getId());
            User freshUser = userService.getUserById(user.getId());
            session.setAttribute("user", freshUser);

            return "redirect:/task/edit?taskId=" + newTask.getId();
        } catch (RuntimeException e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
            return "redirect:/task/list?type=mine";
        }
    }
//    流量削峰
    @GetMapping("/test-limit")
    @ResponseBody
    public String testLimit() {
        if (!rateLimiter.tryAcquire()) {
            return "❌ 拦截成功：瞬时并发流量过大，已触发系统限流保护，请稍后再试！";
        }
        return "✅ 请求成功：系统当前处于安全水位，正常处理业务。";
    }



}