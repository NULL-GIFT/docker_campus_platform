package com.campus_platform.service;

import com.campus_platform.entity.SysLog;
import com.campus_platform.entity.User; // 导包
import java.util.List;
import java.util.Map;

public interface AdminService {
    // 看板数据
    Map<String, Object> getDashboardData();

    //获取用户列表
    List<User> getUserList(Integer status, String keyword);

    //修改用户状态
    void changeUserStatus(Long userId, Integer status, Long operatorId, String operatorName);

    //日志
    List<SysLog> getSystemLogs(String type);
    void recordLog(String type, String content, Long operatorId, String operatorName);
}