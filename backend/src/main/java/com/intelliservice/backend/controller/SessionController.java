package com.intelliservice.backend.controller;

import com.intelliservice.backend.model.dto.ApiResponse;
import com.intelliservice.backend.model.dto.SessionDetailResponse;
import com.intelliservice.backend.model.entity.Session;
import com.intelliservice.backend.service.SessionService;
import com.intelliservice.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;
    private final UserService userService;

    @PostMapping
    public ApiResponse<Session> create(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = currentUserId(userDetails);
        return ApiResponse.success(sessionService.createSession(userId));
    }

    @GetMapping
    public ApiResponse<List<Session>> list(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = currentUserId(userDetails);
        return ApiResponse.success(sessionService.getUserSessions(userId));
    }

    @GetMapping("/{id}")
    public ApiResponse<SessionDetailResponse> detail(@PathVariable Long id) {
        return ApiResponse.success(sessionService.getSessionDetail(id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        sessionService.deleteSession(id);
        return ApiResponse.success(null);
    }

    private Long currentUserId(UserDetails userDetails) {
        return userService.getUserByUsername(userDetails.getUsername()).getId();
    }
}
