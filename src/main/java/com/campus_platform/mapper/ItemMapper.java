package com.campus_platform.mapper;

import com.campus_platform.entity.Item;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ItemMapper {
    int insert(Item item);
    Item selectById(Long id);

    List<Item> selectPage(@Param("offset") int offset, @Param("pageSize") int pageSize);

    List<Item> selectByKeyword(@Param("keyword") String keyword,
                               @Param("offset") int offset,
                               @Param("pageSize") int pageSize);
    int count();

    // 查询我发布的商品
    List<Item> selectMyItems(@Param("userId") Long userId,
                             @Param("offset") int offset,
                             @Param("pageSize") int pageSize);

    //统计我发布的商品数量
    int countMyItems(@Param("userId") Long userId);

     //搜索我发布的商品
    List<Item> selectMyItemsByKeyword(@Param("userId") Long userId,
                                      @Param("keyword") String keyword,
                                      @Param("offset") int offset,
                                      @Param("pageSize") int pageSize);
     //统计我发布的商品数量
    int countMyItemsByKeyword(@Param("userId") Long userId,
                              @Param("keyword") String keyword);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
    int deleteById(@Param("id") Long id);
    int countByKeyword(@Param("keyword") String keyword);




    // 管理员强制下架
    int deleteByAdmin(@Param("id") Long id);
    List<Item> adminFindAll(@Param("statusType") Integer statusType, @Param("keyword") String keyword);
    int countOnSale();    // 统计在售 (status=0, is_deleted=0)
    int countSold();      // 统计已售 (status=1, is_deleted=0)
    int countDeleted();   // 统计已删除 (is_deleted=1)

}