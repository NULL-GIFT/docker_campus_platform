package com.campus_platform.common;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class Md5Util {

    public static String md5(String input) {
        if (input == null || input.trim().isEmpty()) {
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5加密算法不可用", e);
        }
    }

   //验证密码是否正确
   // md5Password 数据库中存储的MD5密码
   // 验证是否匹配

    public static boolean verify(String input, String md5Password) {
        if (input == null || md5Password == null) {
            return false;
        }
        return md5Password.equals(md5(input));
    }


// 测试方法
    public static void main(String[] args) {
        String password = "123456";
        String encrypted = md5(password);
        System.out.println("原始密码: " + password);
        System.out.println("MD5加密: " + encrypted);
        System.out.println("验证结果: " + verify(password, encrypted));
    }
}