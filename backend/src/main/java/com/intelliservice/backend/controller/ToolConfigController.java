package com.intelliservice.backend.controller;

import com.intelliservice.backend.model.dto.ApiResponse;
import com.intelliservice.backend.model.entity.ToolConfig;
import com.intelliservice.backend.service.ToolConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tool-configs")
@RequiredArgsConstructor
public class ToolConfigController {

    private final ToolConfigService toolConfigService;

    /** 查询所有工具配置（仅 admin） */
    @GetMapping
    public ApiResponse<List<ToolConfig>> listAll() {
        return ApiResponse.success(toolConfigService.listAll());
    }

    /** 查询已启用的工具配置（Python Agent 内部调用，无需认证） */
    @GetMapping("/enabled")
    public ApiResponse<List<ToolConfig>> listEnabled() {
        return ApiResponse.success(toolConfigService.listEnabled());
    }

    /** 新增工具配置（仅 admin） */
    @PostMapping
    public ApiResponse<ToolConfig> create(@RequestBody ToolConfig config) {
        return ApiResponse.success(toolConfigService.create(config));
    }

    /** 修改工具配置（仅 admin） */
    @PutMapping("/{id}")
    public ApiResponse<ToolConfig> update(@PathVariable Long id,
                                          @RequestBody ToolConfig config) {
        return ApiResponse.success(toolConfigService.update(id, config));
    }

    /** 启用/禁用工具（仅 admin） */
    @PutMapping("/{id}/toggle")
    public ApiResponse<Void> toggle(@PathVariable Long id,
                                    @RequestBody Map<String, Boolean> body) {
        boolean enabled = Boolean.TRUE.equals(body.get("enabled"));
        toolConfigService.toggleEnabled(id, enabled);
        return ApiResponse.success(null);
    }

    /** 删除工具配置（仅 admin） */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        toolConfigService.delete(id);
        return ApiResponse.success(null);
    }
}
