package com.campus_platform.controller;

import com.campus_platform.common.Result;
import com.campus_platform.entity.SysLog;
import com.campus_platform.entity.User;
import com.campus_platform.service.AdminService;
import com.campus_platform.service.ItemService;
import com.campus_platform.service.NoticeService;
import com.campus_platform.service.TaskService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import com.campus_platform.entity.Task;
import com.campus_platform.entity.Item;

@Controller
public class AdminController {

    @Autowired
    private AdminService adminService;
    // 🔥 新注入这两个 Service
    @Autowired
    private ItemService itemService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private NoticeService noticeService;



    /**
     * 看板
     */
    @GetMapping("/admin/dashboard")
    public String dashboard(HttpSession session, Model model) {
        Map<String, Object> dashboardData = adminService.getDashboardData();
        model.addAllAttributes(dashboardData);
        return "admin/dashboard";
    }

    /**
     * 用户列表
     */
    @GetMapping("/admin/users")
    // required = false 表示这个参数可以不传（比如刚进页面时）
    public String adminUserList(@RequestParam(required = false) Integer status,
                                @RequestParam(required = false) String keyword,
                                Model model) {
        //获取列表
        List<User> userList = adminService.getUserList(status,keyword);
        model.addAttribute("userList", userList);
        model.addAttribute("currentStatus", status);
        model.addAttribute("keyword", keyword);
        model.addAttribute("activeUri", "users");

        return "admin/user_list";
    }

    /**
     * 商品管理列表页
     */
    @GetMapping("/admin/items")
    public String adminItemList(Model model,
                                @RequestParam(required = false) Integer statusType,
                                @RequestParam(required = false) String keyword) {
        List<Item> list = itemService.getAdminItemList(statusType, keyword);
        model.addAttribute("itemList", list);
        model.addAttribute("statusType", statusType);
        model.addAttribute("keyword", keyword);
        model.addAttribute("activeUri", "items");
        return "admin/item_list";
    }

    /**
     * 任务管理列表页
     */
    @GetMapping("/admin/tasks")
    public String adminTaskList(Model model,
                                @RequestParam(required = false) Integer status) {
        List<Task> list = taskService.getAdminTaskList(status);
        model.addAttribute("taskList", list);
        model.addAttribute("currentStatus", status);
        model.addAttribute("activeUri", "tasks");
        return "admin/task_list";
    }

    /**
     * 日志管理列表
     */
    @GetMapping("/admin/logs")
    public String systemLogs(@RequestParam(required = false) String type,
                             Model model) {
        // 查所有日志
        List<SysLog> logList = adminService.getSystemLogs(type);
        model.addAttribute("logList", logList);
        model.addAttribute("currentType", type);
        model.addAttribute("activeUri", "logs");

        return "admin/log_list";
    }



    /**
     * 封号/解封
     */
    @ResponseBody
    @PostMapping("/admin/updateStatus")
    public Result updateStatus(@RequestParam("userId") Long userId,
                               @RequestParam("status") Integer status,
                               HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user.getId().equals(userId)) {
            return Result.error("不能封禁自己的管理员账号");
        }

        //修改状态
        adminService.changeUserStatus(userId, status,user.getId(),user.getNickname());
        return Result.success();
    }

    /**
     * 管理员发布页面 (只有 ADMIN 能进)
     */
    @GetMapping("/admin/notice/publish")
    public String publishPage(HttpSession session) {

        return "notice/publish";
    }


    /**
     * 提交发布接口 (AJAX提交)
     */
    @ResponseBody
    @PostMapping("/admin/notice/publish")
    public Result publish(@RequestParam String title,
                          @RequestParam String content,
                          HttpSession session) {
        User user = (User) session.getAttribute("user");

//        发布公告
        noticeService.publishNotice(title, content, user.getId());
//        记录日志
        adminService.recordLog("NOTICE", "管理员发布了新公告: " + title, user.getId(), user.getNickname());

        return Result.success();
    }

    /**
     * 管理员强制下架商品
     */
    @ResponseBody
    @PostMapping("/admin/item/delete")
    public Result adminDeleteItem(@RequestParam Long itemId, HttpSession session) {
        User user = (User) session.getAttribute("user");
        try {
            // 查标题
            Item item = itemService.getItemById(itemId);
            String title = (item != null) ? item.getTitle() : "未知商品";

            // 删除
            itemService.adminDeleteItem(itemId);

            // 记录日志
            String logContent = "强制下架商品: " + title + " (ID:" + itemId + ")";
            adminService.recordLog("ITEM_DELETE", logContent, user.getId(), user.getNickname());

            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }



    /**
     * 管理员强制删除任务
     */
    @ResponseBody
    @PostMapping("/admin/task/delete")
    public Result adminDeleteTask(@RequestParam Long taskId, HttpSession session) {
        User user = (User) session.getAttribute("user");

        try {
            // 删除前先查出来拿到标题
            Task task = taskService.getTaskDetail(taskId);
            String title = (task != null) ? task.getTitle() : "未知任务";

            taskService.adminDeleteTask(taskId);

            // 修改记录日志时带上标题
            String logContent = "强制删除任务: " + title + " (ID:" + taskId + ")";
            adminService.recordLog("TASK_DELETE", logContent, user.getId(), user.getNickname());

            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }



}