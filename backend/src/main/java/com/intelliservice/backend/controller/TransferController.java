package com.intelliservice.backend.controller;

import com.intelliservice.backend.model.dto.ApiResponse;
import com.intelliservice.backend.model.dto.TransferReplyRequest;
import com.intelliservice.backend.model.dto.TransferServingSessionResponse;
import com.intelliservice.backend.model.dto.TransferSessionRequest;
import com.intelliservice.backend.model.dto.TransferWaitingSessionResponse;
import com.intelliservice.backend.service.SessionService;
import com.intelliservice.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/transfer")
@RequiredArgsConstructor
public class TransferController {

    private final SessionService sessionService;
    private final UserService userService;

    /** 买家请求转人工 */
    @PostMapping("/request")
    public ApiResponse<String> request(@AuthenticationPrincipal UserDetails userDetails,
                                       @RequestBody TransferSessionRequest body) {
        if (body.getSessionId() == null) throw new RuntimeException("缺少 sessionId 参数");
        Long userId = userService.getUserByUsername(userDetails.getUsername()).getId();
        sessionService.requestTransfer(body.getSessionId(), userId);
        return ApiResponse.success("ok");
    }

    /** 客服查询等待接入的会话列表 */
    @GetMapping("/waiting")
    public ApiResponse<List<TransferWaitingSessionResponse>> waiting() {
        return ApiResponse.success(sessionService.getWaitingSessions());
    }

    /** 当前客服正在服务的会话列表 */
    @GetMapping("/serving")
    public ApiResponse<List<TransferServingSessionResponse>> serving(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long agentId = userService.getUserByUsername(userDetails.getUsername()).getId();
        return ApiResponse.success(sessionService.getServingSessions(agentId));
    }

    /** 客服接入会话 */
    @PostMapping("/accept")
    public ApiResponse<String> accept(@AuthenticationPrincipal UserDetails userDetails,
                                      @RequestBody TransferSessionRequest body) {
        if (body.getSessionId() == null) throw new RuntimeException("缺少 sessionId 参数");
        Long agentId = userService.getUserByUsername(userDetails.getUsername()).getId();
        sessionService.acceptTransfer(body.getSessionId(), agentId);
        return ApiResponse.success("ok");
    }

    /** 客服人工回复 */
    @PostMapping("/reply")
    public ApiResponse<String> reply(@AuthenticationPrincipal UserDetails userDetails,
                                     @RequestBody TransferReplyRequest body) {
        if (body.getSessionId() == null) throw new RuntimeException("缺少 sessionId 参数");
        if (body.getMessage() == null || body.getMessage().isBlank()) {
            throw new RuntimeException("缺少 message 参数");
        }
        var currentUser = userService.getUserByUsername(userDetails.getUsername());
        sessionService.replyAsHuman(
                body.getSessionId(),
                currentUser.getId(),
                "admin".equals(currentUser.getRole()),
                body.getMessage()
        );
        return ApiResponse.success("ok");
    }

    /** 客服关闭会话 */
    @PostMapping("/close")
    public ApiResponse<String> close(@AuthenticationPrincipal UserDetails userDetails,
                                     @RequestBody TransferSessionRequest body) {
        if (body.getSessionId() == null) throw new RuntimeException("缺少 sessionId 参数");
        var currentUser = userService.getUserByUsername(userDetails.getUsername());
        sessionService.closeTransfer(
                body.getSessionId(),
                currentUser.getId(),
                "admin".equals(currentUser.getRole())
        );
        return ApiResponse.success("ok");
    }
}
