package com.campus_platform.controller;

import com.campus_platform.common.Result;
import com.campus_platform.entity.Notice;
import com.campus_platform.entity.User;
import com.campus_platform.mapper.AdminMapper;
import com.campus_platform.service.AdminService;
import com.campus_platform.service.NoticeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/notice")
public class NoticeController {

    @Autowired
    private NoticeService noticeService;
    @Autowired
    private AdminService adminService;

    /**
     * 1. 公告列表页 (所有人都能看)
     */
    @GetMapping("/list")
    public String list(Model model) {
        List<Notice> list = noticeService.getNoticeList();
        model.addAttribute("noticeList", list);
        return "notice/list"; // 对应 templates/notice/list.html
    }


}