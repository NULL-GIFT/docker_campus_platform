package com.campus_platform.service;

import com.campus_platform.entity.User;

import java.util.List;

public interface UserService {
    void verifyUser(Long userId, String realName, String studentNo);
    //根据ID获取最新用户数据 (用于刷新Session)

    User getUserById(Long userId);

    void updateUserInfo(User user);
    boolean updatePassword(long userId, String oldPassword, String newPassword);
    // 修改昵称
    void updateNickname(Long userId, String nickname);
    // 充值余额
    void recharge(Long userId, Double amount);
}