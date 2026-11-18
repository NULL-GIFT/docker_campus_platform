package com.campus_platform.service.impl;

import com.campus_platform.entity.Item;
import com.campus_platform.mapper.ItemMapper;
import com.campus_platform.service.ItemService;
import com.campus_platform.common.UploadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ItemMapper itemMapper;

    @Autowired
    private UploadUtil uploadUtil;


    @Override
    public List<Item> getItemList(int page, int pageSize) {
        try {
            // 计算分页偏移量
            int offset = (page - 1) * pageSize;
            // Mapper会自动过滤已删除的商品 (is_deleted = 0)
            return itemMapper.selectPage(offset, pageSize);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public int getTotalPages(int pageSize) {
        try {
            // Mapper只统计未删除的商品
            int total = itemMapper.count();
            if (total == 0) return 1;
            return (int) Math.ceil((double) total / pageSize);
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
    }



    @Override
    public List<Item> getMyItems(Long userId, int page, int pageSize) {
        int offset = (page - 1) * pageSize;
        return itemMapper.selectMyItems(userId, offset, pageSize);
    }

    @Override
    public int getMyItemsTotalPages(Long userId, int pageSize) {
        int total = itemMapper.countMyItems(userId);
        return (int) Math.ceil((double) total / pageSize);
    }



    @Override
    public boolean publishItem(Item item, MultipartFile imageFile) {
        try {
            //  上传图片
            if (imageFile != null && !imageFile.isEmpty()) {
                String imageUrl = uploadUtil.uploadImage(imageFile);
                item.setImageUrl(imageUrl);
            }

            // 设置默认值
            item.setStatus(0); // 0-未售出
            item.setCreateTime(LocalDateTime.now());
            item.setUpdateTime(LocalDateTime.now());
            item.setIsDeleted(0); // 0-未删除

            // 保存到数据库
            int result = itemMapper.insert(item);
            return result > 0;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }



    @Override
    public List<Item> searchItems(String keyword, int page, int pageSize) {
        try {
            int offset = (page - 1) * pageSize;
            // Mapper会自动过滤已删除的商品
            return itemMapper.selectByKeyword(keyword, offset, pageSize);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public int getSearchTotalPages(String keyword, int pageSize) {
        try {
            int total = itemMapper.countByKeyword(keyword);
            if (total == 0) return 1;
            return (int) Math.ceil((double) total / pageSize);
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
    }

    @Override
    public List<Item> searchMyItems(Long userId, String keyword, int page, int pageSize) {
        try {
            int offset = (page - 1) * pageSize;
            // 需要先在 ItemMapper 中添加相应的方法
            return itemMapper.selectMyItemsByKeyword(userId, keyword, offset, pageSize);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public int getSearchMyItemsTotalPages(Long userId, String keyword, int pageSize) {
        try {
            int total = itemMapper.countMyItemsByKeyword(userId, keyword);
            if (total == 0) return 1;
            return (int) Math.ceil((double) total / pageSize);
        } catch (Exception e) {
            e.printStackTrace();
            return 1;
        }
    }



    @Override
    public Item getItemById(Long id) {
        try {
            return itemMapper.selectById(id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    @Override
    @Transactional
    public synchronized boolean updateItemStatus(Long itemId, Integer status) {
        try {
            return itemMapper.updateStatus(itemId, status) > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 普通用户删除商品
  **/
    @Override
    public boolean deleteItem(Long id, Long userId) {
        try {
            //  查商品
            Item item = itemMapper.selectById(id);
            if (item == null) {
                return false;
            }

            // 当前用户必须是卖家
            if (!item.getSellerId().equals(userId)) {
             //   System.err.println("⚠️ 越权警告：用户 " + userId + " 试图删除属于 " + item.getSellerId() + " 的商品！");
                return false;
            }

            // 如果已售出，通常不允许直接删除（防止买家找不到订单信息）
            if (item.getStatus() == 1) {
              //  System.err.println("⚠️ 删除失败：商品已售出，无法删除");
                return false;
            }

            int result = itemMapper.deleteById(id);
            return result > 0;

        } catch (Exception e) {
            e.printStackTrace();

            return false;
        }
    }

    /**
     * 【新增】管理员强制下架实现
     */
    @Override
    public void adminDeleteItem(Long id) {
        // 1. 查商品是否存在
        Item item = itemMapper.selectById(id);
        if (item == null) {
            throw new RuntimeException("商品不存在");
        }

        // 3. 执行强制删除
        int rows = itemMapper.deleteByAdmin(id);
        if (rows == 0) {
            throw new RuntimeException("下架失败，可能已被删除");
        }
    }
    @Override
    public List<Item> getAdminItemList(Integer statusType,String keyword) {
        return itemMapper.adminFindAll(statusType,keyword);
    }



}