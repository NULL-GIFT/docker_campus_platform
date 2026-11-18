package com.campus_platform.service.impl;

import com.campus_platform.entity.Task;
import com.campus_platform.entity.User;
import com.campus_platform.mapper.TaskMapper;
import com.campus_platform.mapper.UserMapper;
import com.campus_platform.service.TaskService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class TaskServiceImpl implements TaskService {

    @Autowired
    private TaskMapper taskMapper;
    @Autowired
    private UserMapper userMapper;
    @Override
    public List<Task> listAvailableTasks() {
        return taskMapper.selectAvailableTasks();
    }
    @Override
    public PageInfo<Task> listAvailableTasks(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        List<Task> list = taskMapper.selectAvailableTasks();
        return new PageInfo<>(list);
    }
    //发布任务
    @Override
    @Transactional
    public void publishTask(Task task, Long publisherId) {

        if (task == null) {
            throw new RuntimeException("任务不能为空");
        }
        if (publisherId == null) {
            throw new RuntimeException("请先登录");
        }
        // ① 托管扣款（关键）  decreaseBalance里面是更新操作，会判断余额，不满足更新行数为0
        int rows = userMapper.decreaseBalance(publisherId, task.getReward());
        if (rows == 0) {
            throw new RuntimeException("余额不足，无法发布任务");
        }
        // ② 插入任务
        task.setStatus(0);
        task.setPublisherId(publisherId);
        task.setTakerId(null);
        // 存入数据库
        taskMapper.insertTask(task);
    }
    // 接单
    @Override
    @Transactional
    public void takeTask(Long taskId, User user) {

        if (taskId == null || user == null) {
            throw new RuntimeException("参数不完整");
        }

        // 1. 实名认证校验
        if (user.getIsVerified() == null || user.getIsVerified() != 1) {
            throw new RuntimeException("未实名认证，无法接单");
        }

        // 2. 查询任务 (这里现在会查出 version 了)
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }

        // 3. 检查任务是否已截止
        LocalDateTime now = LocalDateTime.now();
        if (task.getStatus() == 0 && now.isAfter(task.getDeadline())) {
            throw new RuntimeException("任务已截止，无法接单");
        }

        // 4. 不能接自己发布的任务
        if (task.getPublisherId().equals(user.getId())) {
            throw new RuntimeException("不能接自己发布的任务");
        }

        // 5. 抢单 (乐观锁核心修改)
        // 传入 taskId, userId, 以及刚才查出来的 task.getVersion()
        int rows = taskMapper.takeTask(taskId, user.getId(), task.getVersion());

        // 6. 检查结果
        if (rows == 0) {
            // rows为0 说明 WHERE version = ... 没匹配上，说明被别人抢先改了
            throw new RuntimeException("手慢了，任务已被抢走！");
        }
    }

    @Override
    public Task getTaskDetail(Long taskId) {
        if (taskId == null) {
            throw new RuntimeException("任务ID不能为空");
        }
        Task task = taskMapper.selectById(taskId);
        // 处理接单者姓名格式：姓 + "同学"
        if (task != null && task.getTakerRealName() != null && !task.getTakerRealName().isEmpty()) {
            String realName = task.getTakerRealName();
            // 取姓氏（处理复姓情况，简单取第一个字符）
            String surname = realName.substring(0, 1);
            task.setTakerRealName(surname + "同学");
        }

        return task;
    }

    @Override
    @Transactional
    public void finishTask(Long taskId, Long userId) {

        Task task = taskMapper.selectById(taskId);

        if (task == null) {
            throw new RuntimeException("任务不存在");
        }

        if (!userId.equals(task.getTakerId())) {
            throw new RuntimeException("无权限操作");
        }

        if (task.getStatus() != 1) {
            throw new RuntimeException("任务状态异常");
        }
        // 更新状态 status=2
        int rows = taskMapper.finishTask(taskId, userId);
        if (rows == 0) {
            throw new RuntimeException("任务状态更新失败");
        }
    }
    @Override
    @Transactional
    public void confirmTask(Long taskId, Long userId) {

        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在");
        }
        if (!userId.equals(task.getPublisherId())) {
            throw new RuntimeException("无权限确认");
        }
        if (task.getStatus() != 2) {
            throw new RuntimeException("任务尚未完成");
        }
        // 给接单人打钱(user层)
        userMapper.updateBalance(task.getTakerId(), task.getReward());
        int rows = taskMapper.confirmTask(taskId, userId);
        if (rows == 0) {
            throw new RuntimeException("任务确认失败");
        }
    }


    @Override
    public List<Task> listMyPublishedTasks(Long userId) {
        return taskMapper.selectByPublisher(userId);
    }

    @Override
    public List<Task> listMyTakenTasks(Long userId) {
        return taskMapper.selectByTaker(userId);
    }

    // 完美的删除逻辑（智能判断是否需要退款）
    @Override
    @Transactional
    public void deleteTask(Long taskId, Long userId) {
        Task task = taskMapper.selectById(taskId);
        if (task == null || !task.getPublisherId().equals(userId)) {
            throw new RuntimeException("无权操作");
        }

        if (task.getStatus() == 0) {
            // 【场景A：任务还没过期/定时器还没跑】
            // 必须使用带有 status = 0 的乐观锁去删，防止定时器刚好在这一秒把状态改了
            int rows = taskMapper.deleteTaskWithStatus(taskId, userId, 0);
            if (rows == 0) throw new RuntimeException("操作失败，任务可能已过期或已被接单");

            // 只有成功删除了 status=0 的行，才说明钱还在冻结中，退款给他！
            userMapper.updateBalance(userId, task.getReward());

        } else if (task.getStatus() == -1) {
            // 【场景B：任务已经过期，定时器已经跑过了】
            // 直接标为已删除。坚决不退款！因为定时器早就把钱退过了！
            taskMapper.deleteTaskWithStatus(taskId, userId, -1);
        } else {
            throw new RuntimeException("任务当前状态无法删除");
        }
    }

    // 完美的重新发布逻辑（退旧 + 扣新）
    @Override
    @Transactional
    public Task republishTask(Long oldTaskId, Long userId) {
        Task oldTask = taskMapper.selectById(oldTaskId);
        if (oldTask == null || !oldTask.getPublisherId().equals(userId)) {
            throw new RuntimeException("原任务不存在或无权操作");
        }

        boolean isExpired = (oldTask.getStatus() == -1) ||
                (oldTask.getStatus() == 0 && oldTask.getDeadline() != null && oldTask.getDeadline().isBefore(LocalDateTime.now()));
        if (!isExpired) {
            throw new RuntimeException("只有已过期的任务才能重新发布");
        }

        // 1. 调用刚刚写好的无敌删除逻辑！
        // 如果旧任务钱还没退，它会自动退款；如果定时器退过了，它只删记录。
        this.deleteTask(oldTaskId, userId);

        // 2. 生成新任务
        Task newTask = new Task();
        newTask.setTitle(oldTask.getTitle());
        newTask.setContent(oldTask.getContent());
        newTask.setReward(oldTask.getReward()); // 赏金金额继承
        newTask.setDeadline(LocalDateTime.now().plusDays(7)); // 默认延长7天
        newTask.setStatus(0);
        newTask.setPublisherId(userId);

        // 3. 重新为新任务扣款
        int rows = userMapper.decreaseBalance(userId, newTask.getReward());
        if (rows == 0) {
            throw new RuntimeException("余额不足，无法重新发布");
        }

        taskMapper.insertTask(newTask);
        return newTask;
    }

    @Scheduled(cron = "0 */1 * * * ?")
    // 【注意】这里把 @Transactional 删掉，不要加在循环外面，防止一个出错全盘崩溃
    public void autoCancelExpiredTasks() {
        List<Task> timeoutTasks = taskMapper.selectTimeoutTasks();

        if (timeoutTasks != null && !timeoutTasks.isEmpty()) {
            System.out.println("--- [跑腿大厅] 发现 " + timeoutTasks.size() + " 个过期任务，准备退款 ---");

            for (Task task : timeoutTasks) {
                try {
                    // 1. 先尝试将状态改为 -1。如果 rows > 0，说明成功抢到了这把锁
                    int rows = taskMapper.updateStatusToExpired(task.getId());

                    if (rows > 0) {
                        // 2. 只有成功改成了 -1，才给用户退钱！
                        userMapper.updateBalance(task.getPublisherId(), task.getReward());
                        System.out.println("✅ 成功退款并关闭超时任务 ID: " + task.getId());
                    }
                } catch (Exception e) {
                    System.err.println("❌ 处理超时任务 ID: " + task.getId() + " 失败: " + e.getMessage());
                }
            }
        }
    }

    @Override
    @Transactional
    public void updateTask(Task task, Long userId) {
        // 1. 先从数据库查出旧数据，确认任务是否存在
        Task oldTask = taskMapper.selectById(task.getId());
        if (oldTask == null) {
            throw new RuntimeException("任务不存在");
        }
        // 2. 权限检查：只有发布者本人可以修改
        if (!oldTask.getPublisherId().equals(userId)) {
            throw new RuntimeException("无权操作此任务");
        }
        // 3. 状态检查：只有“待接单(0)”的任务可以修改
        if (oldTask.getStatus() != 0) {
            throw new RuntimeException("任务已被承接或已完成，无法修改信息");
        }
        // 4. 设置更新人ID并执行更新（Mapper里的SQL只更新标题、内容、截止时间）
        task.setPublisherId(userId);
        int rows = taskMapper.updateTask(task);
        if (rows == 0) {
            throw new RuntimeException("系统繁忙，更新失败");
        }
    }





    /**
     * 【新增】管理员强制删除实现
     * 逻辑：
     * 1. 查任务还在不在
     * 2. 把赏金退还给发布者 (Plan A: 温和派做法)
     * 3. 调用 Mapper 的 deleteByAdmin 强行删除
     */
    @Override
    @Transactional
    public void adminDeleteTask(Long taskId) {
        // 1. 先查任务是否存在
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在或已被删除");
        }

        // 2. 退还赏金 (只要任务没被删除，钱肯定还在托管池里，得退给人家)
        // 注意：这里不需要判断 task.getStatus()，只要删了就退钱，防止钱被系统吞了
        if (task.getReward() != null) {
            userMapper.updateBalance(task.getPublisherId(), task.getReward());
        }

        // 3. 执行强制删除 (调用刚才你写的 Mapper 方法)
        int rows = taskMapper.deleteByAdmin(taskId);
        if (rows == 0) {
            throw new RuntimeException("删除失败，可能任务已被其他人操作");
        }
    }
    @Override
    public List<Task> getAdminTaskList(Integer status) {
        return taskMapper.adminFindAll(status);
    }




    // TaskServiceImpl.java
    @Override
    public PageInfo<Task> listMyPublishedTasks(int pageNum, int pageSize, Long userId) {
        PageHelper.startPage(pageNum, pageSize);
        List<Task> list = taskMapper.selectByPublisher(userId);  // 已有方法，按发布时间倒序
        return new PageInfo<>(list);
    }



}
