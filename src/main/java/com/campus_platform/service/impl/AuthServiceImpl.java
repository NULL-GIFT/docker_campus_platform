package com.campus_platform.service.impl;

import com.campus_platform.common.Md5Util;
import com.campus_platform.common.RedisCacheUtil;
import com.campus_platform.common.Result;
import com.campus_platform.entity.SysLog;
import com.campus_platform.entity.User;
import com.campus_platform.mapper.AdminMapper;
import com.campus_platform.mapper.UserMapper;
import com.campus_platform.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private AdminMapper adminMapper;
    @Autowired
    private RedisCacheUtil redisCacheUtil;

    @Override
    public Result login(String username, String password,String inputCode, String token,HttpSession session) {
        // 先校验验证码（这一步最快，放在最前面挡住恶意请求）
        if (token == null || token.isEmpty()) {
            return Result.error("验证码已失效，请刷新");
        }

        String realCode = redisCacheUtil.get(token); // 从本地缓存取

        if (realCode == null) {
            return Result.error("验证码已过期或不存在");
        }

        if (!realCode.equalsIgnoreCase(inputCode)) {
            return Result.error("验证码错误");
        }

        // 【低耦合安全机制】验证码核销成功后立即从 Redis 中移除，确保该 Token 一次性失效，不给重放攻击留空间
        redisCacheUtil.remove(token);

        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
//      判断是否为空
            return Result.error("用户名或密码不能为空");
        }

        User user = userMapper.selectByUsername(username);

        // 判断用户是否存在
        if (user == null) {
            return Result.error("用户不存在或账号已封禁");
        }

        // 判断是否被封号
        if (user.getStatus() != null && user.getStatus() == 0) {
            return Result.error("该账号已被封禁，请联系管理员");
        }

        // 密码校验
        String inputMd5 = Md5Util.md5(password);
        if (!user.getPassword().equals(inputMd5)) {
            return Result.error("密码错误");
        }

        // 登录成功，存入Session
        session.setAttribute("user", user);

        //日志
        try {
            SysLog log = new SysLog();
            log.setOperatorId(user.getId());
            log.setOperatorName(user.getNickname());
            log.setType("LOGIN");
            log.setContent("登录成功");

            adminMapper.insertLog(log);
        } catch (Exception e) {
            // 在控制台打印错误
            System.err.println("⚠️ 警告：日志记录失败，但不影响登录。原因：" + e.getMessage());
            e.printStackTrace();
        }

        return Result.success(user);
    }

    //注册——用户名不能相同
    public Result register(String username, String password) {
        int count = userMapper.countByUsername(username);
        if (count > 0) {
            return Result.error("该用户名已存在");
        }


//        注入用户信息
        User user = new User();
        user.setUsername(username);
        user.setPassword(Md5Util.md5(password));
        user.setNickname("同学" + System.currentTimeMillis() % 10000);
        user.setIsVerified(0);
        user.setStatus(1);
        user.setRole("USER");
        user.setBalance(java.math.BigDecimal.ZERO);
        user.setCreateTime(java.time.LocalDateTime.now());
        user.setUpdateTime(java.time.LocalDateTime.now());

        userMapper.insert(user);
        return Result.success();
    }
}