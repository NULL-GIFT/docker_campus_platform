package com.campus_platform.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Notice {
    private Long id;
    private String title;
    private String content;
    private Long publisherId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

}