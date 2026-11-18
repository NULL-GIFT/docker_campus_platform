package com.campus_platform.config;


import com.campus_platform.interceptor.AdminInterceptor;
import com.campus_platform.interceptor.LoginInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;
/**
 * Web配置类
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private LoginInterceptor loginInterceptor;
    @Autowired
    private AdminInterceptor adminInterceptor;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/**")        // 拦截所有路径
                .excludePathPatterns(          // 排除以下路径（白名单）
                        "/auth/**",            // 登录注册相关
                        "/static/**",          // 静态资源
                        "/css/**", "/js/**", "/images/**", "/upload/**",
                        "/error",
                        "/actuator/**",        // ⚠️ 新增：给 K8s 探针留出绿色通道
                        "/chaos/**" ,          // ⚠️ 新增：给混沌测试演练接口留出通道
                        "/favicon.ico"
                );
        // 第二道防线：查是不是管理员
        registry.addInterceptor(adminInterceptor)
                .addPathPatterns("/admin/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 【工程化改造】
        // 1. 获取物理绝对路径
        // 2. 必须加上 "file:" 协议头，告诉 Spring 这是一个外部磁盘路径，不是包内资源
        // 3. 必须确保最后有一个斜杠结尾
        String location = "file:" + Paths.get(uploadDir).toAbsolutePath().normalize().toString() + "/";

        // 告诉 Nginx/Tomcat：只要是访问 /upload/ 开头的网络请求，全都去这个外部磁盘目录拿
        registry.addResourceHandler("/upload/**")
                .addResourceLocations(location);
    }
}