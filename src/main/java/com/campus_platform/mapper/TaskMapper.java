package com.campus_platform.mapper;

import com.campus_platform.entity.Task;

import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface TaskMapper {

    /**
     * 查询所有可接单任务（状态 = 0，未删除）
     */
    List<Task> selectAvailableTasks();
    /**
     * 发布任务
     */
    int insertTask(Task task);

    /**
     * 抢单（带 takeTime 版本）
     */
    int takeTask(@Param("taskId") Long taskId,
                 @Param("takerId") Long takerId,
                 @Param("version") Integer version);

    /**
     * 根据 ID 查询任务详情
     */
    Task selectById(Long taskId);

    // 接单人完成任务
    int finishTask(@Param("taskId") Long taskId,
                   @Param("takerId") Long takerId);

    // 发布者确认完成
    int confirmTask(@Param("taskId") Long taskId,
                    @Param("publisherId") Long publisherId);
    // 个人主页上能够显示个人抢的单以及自己发布的单子
    List<Task> selectByPublisher(@Param("publisherId") Long publisherId);

    List<Task> selectByTaker(@Param("takerId") Long takerId);

    // 修改任务
    int updateTask(Task task);

    // 严谨的带有状态锁的逻辑删除
    int deleteTaskWithStatus(@Param("taskId") Long taskId,
                             @Param("publisherId") Long publisherId,
                             @Param("status") Integer status);



    /**
     * 🔥【新增】管理员强制删除：只验证 ID，不验证发布者
     */
    int deleteByAdmin(@Param("taskId") Long taskId);
    List<Task> adminFindAll(@Param("status") Integer status);


    // 查询所有状态为0（待接单）且已经过了截止时间的任务
    List<Task> selectTimeoutTasks();
    // 将超时任务的状态变更为 -1 (已失效/退款)
    int updateStatusToExpired(Long taskId);

}
