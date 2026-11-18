package com.campus_platform.service;
import com.campus_platform.common.Result;
import jakarta.servlet.http.HttpSession;

public interface AuthService {
    Result login(String username, String password, String inputCode, String token,HttpSession session);
}
