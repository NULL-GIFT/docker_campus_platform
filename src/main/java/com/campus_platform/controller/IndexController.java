//package com.campus_platform.controller;
//
//import com.campus_platform.entity.User;
//import jakarta.servlet.http.HttpSession;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.GetMapping;
//
//@Controller
//public class IndexController {
//
//    /**
//     * 系统首页（大厅）
//     * URL: /
//     */
//    @GetMapping("/")
//    public String index(HttpSession session, Model model) {
//        // 1. 从 Session 获取当前登录用户
//        User user = (User) session.getAttribute("user");
//
//        // 2. 如果没登录，踢回登录页（虽然拦截器以后会做，但这里双重保险）
//        if (user == null) {
//            return "redirect:/auth/login";
//        }
//
//        // 3. 把用户信息传给前端，方便显示 "欢迎你，XXX"
//        model.addAttribute("user", user);
//
//        // 4. 跳转到 templates/index.html
//        return "index";
//    }
//}

package com.campus_platform.controller;

import com.campus_platform.entity.User; // ★★★ 必须确认是这个包！不要导成别的了
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Enumeration;

@Controller
public class IndexController {

    @GetMapping("/")
    public String index(HttpSession session, Model model) {
        System.out.println("========== 进入大厅(IndexController) ==========");

        // 1. 打印 Session 里到底有啥，帮我们找原因
        Enumeration<String> attributeNames = session.getAttributeNames();
        boolean hasKey = false;
        while (attributeNames.hasMoreElements()) {
            String key = attributeNames.nextElement();
            System.out.println("Session里发现钥匙: " + key);
            if ("user".equals(key)) {
                hasKey = true;
            }
        }

        // 2. 尝试取出用户
        Object userObj = session.getAttribute("user");
        System.out.println("取出的 user 对象: " + userObj);

        if (userObj == null) {
            System.out.println("Result: 用户为空，踢回登录页！(请检查 AuthServiceImpl 是否真的存了 'user')");
            return "redirect:/auth/login";
        }

        // 3. 安全转换
        if (userObj instanceof User) {
            User user = (User) userObj;
            model.addAttribute("user", user);
            System.out.println("Result: 验证通过，欢迎用户: " + user.getNickname());
            return "index";
        } else {
            System.out.println("Result: 类型不匹配！Session里存的不是 com.campus_platform.entity.User");
            // 这种情况通常是热部署导致的，重启一下就好
            session.invalidate(); // 清空错误的 session
            return "redirect:/auth/login";
        }
    }
}