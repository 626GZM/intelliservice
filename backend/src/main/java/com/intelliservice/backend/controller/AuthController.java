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

    @GetMapping("/me")
    public ApiResponse<UserResponse> me(@AuthenticationPrincipal UserDetails userDetails) {
        var user = userService.getUserByUsername(userDetails.getUsername());
        return ApiResponse.success(UserResponse.from(user));
    }
}
