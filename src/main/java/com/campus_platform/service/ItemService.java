package com.campus_platform.service;

import com.campus_platform.entity.Item;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface ItemService {
    boolean publishItem(Item item, MultipartFile imageFile);
    List<Item> getItemList(int page, int pageSize);
    int getTotalPages(int pageSize);
    List<Item> searchItems(String keyword, int page, int pageSize);
    Item getItemById(Long id);


    List<Item> getMyItems(Long userId, int page, int pageSize);
    int getMyItemsTotalPages(Long userId, int pageSize);
    List<Item> searchMyItems(Long userId, String keyword, int page, int pageSize);
    int getSearchMyItemsTotalPages(Long userId, String keyword, int pageSize);

    boolean updateItemStatus(Long itemId, Integer status);
    boolean deleteItem(Long id,Long userId);

    // 【新增】管理员强制下架
    void adminDeleteItem(Long id);
    List<Item> getAdminItemList(Integer statusType, String keyword);

    int getSearchTotalPages(String keyword, int pageSize);
}