package com.intelliservice.backend.controller;

import com.intelliservice.backend.model.dto.ApiResponse;
import com.intelliservice.backend.model.dto.ChatRequest;
import com.intelliservice.backend.model.dto.ChatResponse;
import com.intelliservice.backend.service.ChatService;
import com.intelliservice.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final UserService userService;

    @PostMapping
    public ApiResponse<ChatResponse> chat(@AuthenticationPrincipal UserDetails userDetails,
                                          @RequestBody ChatRequest request) {
        Long userId = userService.getUserByUsername(userDetails.getUsername()).getId();
        return ApiResponse.success(chatService.chat(userId, request));
    }
}
