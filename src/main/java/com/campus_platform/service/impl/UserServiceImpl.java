package com.campus_platform.service.impl;

import com.campus_platform.common.Md5Util;
import com.campus_platform.entity.User;
import com.campus_platform.mapper.UserMapper;
import com.campus_platform.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
    // 实名认证业务
    @Override
    @Transactional
    public void verifyUser(Long userId, String realName, String studentNo) {

        // 1. 业务校验
        if (realName == null || realName.trim().isEmpty()) {
            throw new RuntimeException("真实姓名不能为空");
        }
        if (studentNo == null || studentNo.trim().isEmpty()) {
            throw new RuntimeException("学号不能为空");
        }

        // 2. 检查学号是否已被其他用户使用
        User existingUser = userMapper.selectByStudentNo(studentNo);
        if (existingUser != null && !existingUser.getId().equals(userId)) {
            throw new RuntimeException("该学号已被其他用户使用，请重新输入");
        }

        // 3. 校验是否已经认证过，防止重复提交
        boolean isVerified = userMapper.checkVerified(userId);
        if (isVerified) {
            throw new RuntimeException("您已完成认证，无需重复提交");
        }

        // 4. 调用 Mapper 更新数据库
        int rows = userMapper.updateVerification(userId, realName, studentNo);
        if (rows == 0) {
            throw new RuntimeException("认证失败，用户不存在");
        }
    }

    @Override
    public User getUserById(Long userId) {
        return userMapper.selectById(userId);
    }

    @Override
    public void updateUserInfo(User user) {
        userMapper.updateUser(user);
    }

    @Override
    @Transactional
    public boolean updatePassword(long userId, String oldPassword, String newPassword) {
        // 对密码进行MD5加密
        String oldPasswordMd5 = Md5Util.md5(oldPassword);
        String newPasswordMd5 = Md5Util.md5(newPassword);

        // 检查新密码是否与旧密码相同
        if (oldPasswordMd5.equals(newPasswordMd5)) {
            throw new RuntimeException("新密码不能与旧密码相同，请重新设置");
        }

        // 调用Mapper更新密码，返回受影响的行数
        int affectedRows = userMapper.updatePassword(userId, oldPasswordMd5, newPasswordMd5);
        return affectedRows > 0;
    }

    // 修改昵称
    @Override
    public void updateNickname(Long userId, String nickname) {
        if (nickname == null || nickname.trim().isEmpty()) {
            throw new RuntimeException("昵称不能为空");
        }
        userMapper.updateNickname(userId, nickname);
    }

    // 充值
    @Override
    @Transactional
    public void recharge(Long userId, Double amount) {
        if (amount == null || amount <= 0) {
            throw new RuntimeException("充值金额必须大于0");
        }
        // 使用 BigDecimal 转换，避免精度丢失
        java.math.BigDecimal money = java.math.BigDecimal.valueOf(amount);

        // 复用 XML 里已有的 updateBalance (它是做加法逻辑)
        userMapper.updateBalance(userId, money);
    }
}