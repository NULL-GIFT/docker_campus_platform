package com.campus_platform.service.impl;

import com.campus_platform.entity.SysLog;
import com.campus_platform.entity.User;
import com.campus_platform.mapper.AdminMapper;
import com.campus_platform.mapper.ItemMapper;
import com.campus_platform.mapper.UserMapper;
import com.campus_platform.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private AdminMapper adminMapper;
    @Autowired
    private ItemMapper itemMapper;
    @Autowired
    private UserMapper userMapper;


//    首页数据统计
    @Override
    public Map<String, Object> getDashboardData() {
        Map<String, Object> data = new HashMap<>();

//        获取基础统计数据
        Integer users = adminMapper.countTotalUsers();  // 总用户数
        Integer tasks = adminMapper.countActiveTasks(); // 活跃任务数
        java.math.BigDecimal amount = adminMapper.sumTotalAmount();  // 总交易额
        // 查询商品分布数据
        int statOnSale = itemMapper.countOnSale();  // 在售商品数
        int statSold = itemMapper.countSold();      // 已售商品数
        int statDeleted = itemMapper.countDeleted();  // 已删商品数

        // 放入Map，传给前端
        // BigDecimal是对象类型，在SQL返回null时会是null
        // 如果数据库是空的，将null设为0
        data.put("totalUsers", users != null ? users : 0);
        data.put("activeTasks", tasks != null ? tasks : 0);
        data.put("totalAmount", amount != null ? amount : 0.00);
        data.put("statOnSale", statOnSale);
        data.put("statSold", statSold);
        data.put("statDeleted", statDeleted);

        // 折线图数据
        // X轴：日期   Y轴：数量
        List<Map<String, Object>> dbTrendList = adminMapper.countUserTrend();
        List<String> dateList = new ArrayList<>();
        List<Long> countList = new ArrayList<>();

        // 如果数据库完全没数据，防止报错，还是给个空列表
        if (dbTrendList != null) {
            for (Map<String, Object> map : dbTrendList) {
//                从Map中获取的Object类型转为String类型
                String d = String.valueOf(map.get("dateStr"));
                // 转成Long防止类型转换错误
                Long c = ((Number) map.get("count")).longValue();

                dateList.add(d);
                countList.add(c);
            }
        }

        data.put("chartDate", dateList);
        data.put("chartData", countList);

        return data;
    }


// 用户列表查询
    @Override
    public List<User> getUserList(Integer status,String keyword) {

        return adminMapper.findAll(status,keyword);
    }

//封号/解封用户
@Override
public void changeUserStatus(Long userId, Integer status, Long operatorId, String operatorName) {
    // 状态更新
    adminMapper.updateStatus(userId, status);

    // 查询目标用户信息记日志
    User targetUser = userMapper.selectById(userId);

    // 拼接详细日志内容
    StringBuilder content = new StringBuilder();
    content.append(status == 0 ? "封禁用户: " : "解封用户: ");

    if (targetUser != null) {
        // 昵称(账号)
        content.append(targetUser.getNickname())
                .append(" (").append(targetUser.getUsername()).append(")");

        // 实名信息
        if (targetUser.getIsVerified() != null && targetUser.getIsVerified() == 1) {
            content.append(" [实名:").append(targetUser.getRealName())
                    .append("|").append(targetUser.getStudentNo()).append("]");
        } else {
            content.append(" [未实名]");
        }
    } else {
        // 防御性代码，万一查不到
        content.append("ID:").append(userId);
    }

    // 记录日志
    String logType = (status == 0) ? "BAN" : "UNBAN";
    recordLog(logType, content.toString(), operatorId, operatorName);
}


//获取系统日志
    @Override
    public List<SysLog> getSystemLogs(String type) {
        // 如果前端传 "ALL" 或者空，就转成 null 传给 Mapper
        if ("ALL".equals(type) || "".equals(type)) {
            type = null;
        }
        return adminMapper.selectAllLogs(type);
    }

    @Override
    public void recordLog(String type, String content, Long operatorId, String operatorName) {
        SysLog log = new SysLog();
        log.setType(type);
        log.setContent(content);
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        adminMapper.insertLog(log);
    }
}