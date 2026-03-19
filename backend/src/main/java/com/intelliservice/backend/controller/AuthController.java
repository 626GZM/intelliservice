package com.intelliservice.backend.controller;

import com.intelliservice.backend.model.dto.ApiResponse;
import com.intelliservice.backend.model.dto.LoginRequest;
import com.intelliservice.backend.model.dto.RegisterRequest;
import com.intelliservice.backend.model.dto.UserResponse;
import com.intelliservice.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ApiResponse<UserResponse> register(@RequestBody RegisterRequest request) {
        var user = userService.register(request);
        return ApiResponse.success(UserResponse.from(user));
    }

    @PostMapping("/login")
    public ApiResponse<Map<String, String>> login(@RequestBody LoginRequest request) {
        String token = userService.login(request);
        return ApiResponse.success(Map.of("token", token));
    }

    /** 获取当前登录用户信息（含 role 字段） */
    @GetMapping("/me")
    public ApiResponse<UserResponse> me(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ApiResponse.error(401, "未登录");
        }
        var user = userService.getUserByUsername(userDetails.getUsername());
        return ApiResponse.success(UserResponse.from(user));
    }

    /**
     * 注册管理员账号 — 仅开发/初始化环境使用。
     * 生产环境请直接执行 SQL 或删除此接口。
     */
    @PostMapping("/register-admin")
    public ApiResponse<UserResponse> registerAdmin(@RequestBody RegisterRequest request) {
        var user = userService.registerAdmin(request);
        return ApiResponse.success(UserResponse.from(user));
    }
}
