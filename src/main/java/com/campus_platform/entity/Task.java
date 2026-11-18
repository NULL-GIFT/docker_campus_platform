package com.campus_platform.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Task {

    private Long id;

    private String title;

    private String content;

    private BigDecimal reward;

    private LocalDateTime deadline;

    private Integer status;

    private Long publisherId;

    private Long takerId;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    private Integer isDeleted;

    // 乐观锁版本号
    private Integer version;


    // 显示字段（不需要存到数据库，只是查询结果映射）
    private String publisherNickname;  // 发布者昵称
    private String takerRealName;      // 接单者真实姓名
    private boolean expired;

}
