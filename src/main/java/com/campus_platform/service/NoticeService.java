package com.campus_platform.service;

import com.campus_platform.entity.Notice;
import java.util.List;

public interface NoticeService {
    void publishNotice(String title, String content, Long userId);
    List<Notice> getNoticeList();
}