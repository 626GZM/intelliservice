package com.intelliservice.backend.controller;

import com.intelliservice.backend.model.dto.ApiResponse;
import com.intelliservice.backend.model.entity.AgentConfig;
import com.intelliservice.backend.service.AgentConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/agent-configs")
@RequiredArgsConstructor
public class AgentConfigController {

    private final AgentConfigService agentConfigService;

    /** 查询所有 Agent 配置（仅 admin） */
    @GetMapping
    public ApiResponse<List<AgentConfig>> listAll() {
        return ApiResponse.success(agentConfigService.listAll());
    }

    /** 查询已启用的 Agent 配置（Python Agent 内部调用，无需认证） */
    @GetMapping("/enabled")
    public ApiResponse<List<AgentConfig>> listEnabled() {
        return ApiResponse.success(agentConfigService.listEnabled());
    }

    /** 新增 Agent 配置（仅 admin） */
    @PostMapping
    public ApiResponse<AgentConfig> create(@RequestBody AgentConfig config) {
        return ApiResponse.success(agentConfigService.create(config));
    }

    /** 修改 Agent 配置（仅 admin） */
    @PutMapping("/{id}")
    public ApiResponse<AgentConfig> update(@PathVariable Long id,
                                           @RequestBody AgentConfig config) {
        return ApiResponse.success(agentConfigService.update(id, config));
    }

    /** 启用/禁用 Agent（仅 admin） */
    @PutMapping("/{id}/toggle")
    public ApiResponse<Void> toggle(@PathVariable Long id,
                                    @RequestBody Map<String, Boolean> body) {
        boolean enabled = Boolean.TRUE.equals(body.get("enabled"));
        agentConfigService.toggleEnabled(id, enabled);
        return ApiResponse.success(null);
    }

    /** 删除 Agent 配置（仅 admin） */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        agentConfigService.delete(id);
        return ApiResponse.success(null);
    }
}
