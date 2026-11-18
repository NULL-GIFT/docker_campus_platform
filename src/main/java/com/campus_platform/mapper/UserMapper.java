package com.campus_platform.mapper;

import com.campus_platform.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

@Mapper
public interface UserMapper {
    User selectByUsername(@Param("username") String username);
    User selectById(@Param("id") Long id);
    // 根据学号查询用户
    User selectByStudentNo(@Param("studentNo") String studentNo);
    int insert(User user);
    int updateUser(User user);
    int updateVerification(@Param("userId") Long userId,
                           @Param("realName") String realName,
                           @Param("studentNo") String studentNo);
    int updateBalance(@Param("userId") Long userId, @Param("amount") BigDecimal amount);
    int countByUsername(@Param("username") String username);
    int updatePassword(@Param("userId") Long userId,
                       @Param("oldPassword") String oldPassword,
                       @Param("newPassword") String newPassword);

    //安全扣减余额
    int decreaseBalance(@Param("userId") Long userId,
                        @Param("amount") BigDecimal amount);

    BigDecimal getBalance(@Param("userId") Long userId);
    boolean checkVerified(@Param("userId") Long userId);

    // 修改昵称
    int updateNickname(@Param("userId") Long userId, @Param("nickname") String nickname);
}