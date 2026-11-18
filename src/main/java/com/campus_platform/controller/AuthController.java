package com.campus_platform.controller;

import cn.hutool.captcha.CaptchaUtil;
import cn.hutool.captcha.LineCaptcha;
import cn.hutool.core.lang.UUID;
import com.campus_platform.common.RedisCacheUtil;
import com.campus_platform.common.Result;
import com.campus_platform.service.impl.AuthServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthServiceImpl authService;
    @Autowired
    private RedisCacheUtil redisCacheUtil;

    // 登录页面
    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    // 注册页面
    @GetMapping("/register")
    public String registerPage() {
        return "auth/register";
    }

    // 登录接口
    // 前端通过Ajax发送POST请求到接口
    @ResponseBody
    @PostMapping("/login")
    public Result login(@RequestParam String username,
                        @RequestParam String password,
                        @RequestParam String inputCode, // 接收用户输入的验证码
                        @RequestParam String token,     // 接收验证码的身份证Token
                        HttpSession session) {
        return authService.login(username, password, inputCode, token, session);
    }

    // 注册接口
    // @ResponseBody不要返回页面，而是把返回值转换为JSON格式返回给客户端
    @ResponseBody
    @PostMapping("/register")
    public Result register(@RequestParam String username, @RequestParam String password) {
        return authService.register(username, password);
    }



    @GetMapping("/captcha")
    @ResponseBody
    public Result getCaptcha() {
        // 使用Hutool生成验证码图片和文字
        // 定义图形验证码的长和宽,验证码字符数，干扰线元素个数
        LineCaptcha lineCaptcha = CaptchaUtil.createLineCaptcha(200, 100, 4, 20);

        String code = lineCaptcha.getCode();
        String imageBase64 = lineCaptcha.getImageBase64(); // 图片的Base64编码

        // 生成一个Token(身份证)
        String token = UUID.randomUUID().toString();

        // 存入本地缓存，60秒过期
        // Key是Token，Value是验证码
        redisCacheUtil.set(token, code, 60);

        // 返回给前端
        Map<String, String> map = new HashMap<>();
        map.put("token", token);
//        只有加上这个前缀，前端的 <img src="..."> 标签才能直接识别并显示图片，否则就是一堆乱码。
        map.put("img", "data:image/png;base64," + imageBase64);

        return Result.success(map);
    }



    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/auth/login";
    }



    @GetMapping("/check")
    public ResponseEntity<?> checkAuth(HttpServletRequest request, HttpSession session) {
        //Nginx必须通过X-Original-URI告诉后端：用户最初到底想访问哪个地址
        String originalUri = request.getHeader("X-Original-URI");

        if (originalUri == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 未知请求直接击毙
        }

        //放行登录注册、验证码或静态资源
        if (originalUri.startsWith("/auth/") ||
                originalUri.startsWith("/static/") ||
                originalUri.startsWith("/css/") ||
                originalUri.startsWith("/js/") ||
                originalUri.startsWith("/images/") ||
                originalUri.startsWith("/upload/") ||
                originalUri.equals("/favicon.ico") ||
                originalUri.startsWith("/actuator/")) {
            return ResponseEntity.ok().build(); // 返回 HTTP 200，Nginx 开闸放水
        }

        //查验Session，Session已经被底层配置接管，存储在Redis集群里
        if (session.getAttribute("user") != null) {
            return ResponseEntity.ok().build(); // Redis 确认已登录，返回 200 放行
        }


        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }



}