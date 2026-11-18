package com.campus_platform.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SysLog {
    private Long id;
    private Long operatorId;      // 操作人ID
    private String operatorName;  // 操作人名字
    private String type;          // 操作类型
    private String content;       // 内容
    private LocalDateTime createTime;
}