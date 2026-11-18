package com.campus_platform.service.impl;

import com.campus_platform.entity.Notice;
import com.campus_platform.mapper.NoticeMapper;
import com.campus_platform.service.NoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NoticeServiceImpl implements NoticeService {

    @Autowired
    private NoticeMapper noticeMapper;

    @Override
    public void publishNotice(String title, String content, Long userId) {
        Notice notice = new Notice();
        notice.setTitle(title);
        notice.setContent(content);
        notice.setPublisherId(userId); // 记录是谁发的(管理员ID)

        noticeMapper.insert(notice);
    }

    @Override
    public List<Notice> getNoticeList() {
        return noticeMapper.findAll();
    }
}