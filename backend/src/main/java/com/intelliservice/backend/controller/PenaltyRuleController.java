package com.intelliservice.backend.controller;

import com.intelliservice.backend.model.dto.ApiResponse;
import com.intelliservice.backend.model.entity.PenaltyRule;
import com.intelliservice.backend.service.PenaltyRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/penalty-rules")
@RequiredArgsConstructor
public class PenaltyRuleController {

    private final PenaltyRuleService penaltyRuleService;

    /** 获取所有判罚规则（放行，供 Python Agent 调用） */
    @GetMapping
    public ApiResponse<List<PenaltyRule>> list() {
        return ApiResponse.success(penaltyRuleService.listAll());
    }
}
