package com.campus_platform.mapper;

import com.campus_platform.entity.SysLog;
import com.campus_platform.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface AdminMapper {
    //统计总用户
    int countTotalUsers();

    //统计活跃任务
    int countActiveTasks();

    //统计总交易额
    BigDecimal sumTotalAmount();

    //统计近7天的用户增长,返回Map列表
    List<Map<String, Object>> countUserTrend();


    // 用户管理---查所有用户列表
    List<User> findAll(@Param("status") Integer status, @Param("keyword") String keyword);


    // 日志相关方法
    // 插入一条日志
    int insertLog(SysLog sysLog);

    //查询所有日志
    List<SysLog> selectAllLogs(@Param("type") String type);

    // 封号/解封
    int updateStatus(@Param("userId") Long userId, @Param("status") Integer status);


}