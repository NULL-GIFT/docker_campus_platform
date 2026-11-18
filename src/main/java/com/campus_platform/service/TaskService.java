package com.campus_platform.service;

import com.campus_platform.entity.Task;
import com.campus_platform.entity.User;
import com.github.pagehelper.PageInfo;

import java.util.List;

public interface TaskService {
    List<Task> listAvailableTasks();

    void publishTask(Task task, Long publisherId);

     void takeTask(Long taskId, User user);

    Task getTaskDetail(Long taskId);

//    接单人完成任务
    void finishTask(Long taskId, Long userId);
    // 发布者点击“确认完成”
    void confirmTask(Long taskId, Long userId);

    List<Task> listMyPublishedTasks(Long userId);

    List<Task> listMyTakenTasks(Long userId);

    void updateTask(Task task, Long userId);
    void deleteTask(Long taskId, Long userId);

    // 🔥【新增】管理员强制删除任务（无视发布者身份）
    void adminDeleteTask(Long taskId);
    List<Task> getAdminTaskList(Integer status);


    // 修改原有的 listAvailableTasks 方法签名
    // pageNum: 当前第几页, pageSize: 每页多少条
    PageInfo<Task> listAvailableTasks(int pageNum, int pageSize);

    // TaskService.java
    PageInfo<Task> listMyPublishedTasks(int pageNum, int pageSize, Long userId);
    Task republishTask(Long oldTaskId, Long userId);

}
