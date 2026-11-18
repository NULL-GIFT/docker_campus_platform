package com.campus_platform.mapper;

import com.campus_platform.entity.Notice;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface NoticeMapper {
    // 1. 发布公告
    int insert(Notice notice);

    // 2. 查询所有公告（给所有人看）
    List<Notice> findAll();

    // 3. 查详情（可选）
    Notice selectById(Long id);
}