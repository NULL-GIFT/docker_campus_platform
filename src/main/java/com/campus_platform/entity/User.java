package com.campus_platform.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 用户实体类
 * 对应数据库 user 表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable{

    // 声明序列化版本号，防止后续类结构调整时反序列化失败
    private static final long serialVersionUID = 1L;

    private Long id;                    // 用户ID
    private String username;            // 登录账号
    private String password;            // 登录密码（加密）
    private String nickname;            // 昵称
    private String realName;            // 真实姓名
    private String studentNo;           // 学号
    private Integer isVerified;         // 实名认证状态：0未认证 1已认证
    private Integer status;             // 账号状态：1正常 0封号
    private String role;                // 角色：USER / ADMIN
    private BigDecimal balance;         // 余额
    private LocalDateTime createTime;   // 注册时间
    private LocalDateTime updateTime;   // 更新时间

//    管理员业务
    private Integer publishedTaskCount; // 发布任务数
    private Integer takenTaskCount;     // 接受任务数
    private Integer itemSoldCount;      // 上架/卖出商品数 (发布数)
    private Integer itemBoughtCount;    // 买入商品数 (订单数)




    // 用于测试
//    public User(String username, String password, String nickname) {
//        this.username = username;
//        this.password = password;
//        this.nickname = nickname;
//        this.isVerified = 0;
//        this.status = 1;
//        this.role = "USER";
//        this.balance = BigDecimal.ZERO;
//        this.createTime = LocalDateTime.now();
//        this.updateTime = LocalDateTime.now();
//    }


    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", nickname='" + nickname + '\'' +
                ", realName='" + realName + '\'' +
                ", studentNo='" + studentNo + '\'' +
                ", isVerified=" + isVerified +
                ", status=" + status +
                ", role='" + role + '\'' +
                ", balance=" + balance +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                '}';
    }


}