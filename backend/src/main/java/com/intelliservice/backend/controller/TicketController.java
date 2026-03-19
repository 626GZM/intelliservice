package com.intelliservice.backend.controller;

import com.intelliservice.backend.model.dto.ApiResponse;
import com.intelliservice.backend.model.dto.ReviewTicketRequest;
import com.intelliservice.backend.model.entity.Ticket;
import com.intelliservice.backend.service.TicketService;
import com.intelliservice.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;
    private final UserService userService;

    /** 所有工单（agent/admin） */
    @GetMapping
    public ApiResponse<List<Ticket>> list(@RequestParam(required = false) String status) {
        return ApiResponse.success(ticketService.listAll(status));
    }

    @GetMapping("/{id}")
    public ApiResponse<Ticket> detail(@PathVariable Long id) {
        return ApiResponse.success(ticketService.getTicketById(id));
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Ticket> updateStatus(@PathVariable Long id,
                                            @RequestBody Map<String, String> body) {
        return ApiResponse.success(ticketService.updateTicketStatus(id, body.get("status")));
    }

    /**
     * Python Agent 回调：保存 AI 建议的处罚方案。
     * 路径已在 SecurityConfig 中放行，无需 JWT。
     */
    @PutMapping("/{id}/ai-suggest")
    public ApiResponse<Ticket> aiSuggest(@PathVariable Long id,
                                         @RequestBody Map<String, Object> body) {
        String suggestion;
        try {
            suggestion = body.containsKey("ai_suggestion")
                    ? ticketService.toJson(body.get("ai_suggestion"))
                    : ticketService.toJson(body);
        } catch (RuntimeException ex) {
            throw new RuntimeException("AI 判罚建议格式错误");
        }
        return ApiResponse.success(ticketService.saveAiSuggestion(id, suggestion));
    }

    /** 客服审阅：approved=true/false，可附带 finalPenalty */
    @PutMapping("/{id}/review")
    public ApiResponse<Ticket> review(@PathVariable Long id,
                                      @AuthenticationPrincipal UserDetails userDetails,
                                      @RequestBody ReviewTicketRequest req) {
        Long reviewerId = userService.getUserByUsername(userDetails.getUsername()).getId();
        return ApiResponse.success(ticketService.review(id, reviewerId, req));
    }

    /** 执行处罚 → 更新商户 score 和 total_fines */
    @PutMapping("/{id}/execute")
    public ApiResponse<Ticket> execute(@PathVariable Long id) {
        return ApiResponse.success(ticketService.executePenalty(id));
    }

    /** 结算工单 → 状态改为 closed */
    @PutMapping("/{id}/settle")
    public ApiResponse<Ticket> settle(@PathVariable Long id) {
        return ApiResponse.success(ticketService.settle(id));
    }
}
