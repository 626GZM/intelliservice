package com.intelliservice.backend.controller;

import com.intelliservice.backend.service.McpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/mcp")
@RequiredArgsConstructor
public class McpController {

    private final McpService mcpService;

    /** 返回可用工具列表 */
    @GetMapping("/tools")
    public ResponseEntity<Map<String, Object>> tools() {
        List<Map<String, Object>> tools = List.of(
            buildTool(
                "query_order",
                "根据订单号查询订单状态和物流信息",
                Map.of(
                    "order_id", Map.of("type", "string", "description", "订单号，如 ORD-88892")
                ),
                List.of("order_id")
            ),
            buildTool(
                "query_customer",
                "根据客户ID查询客户基本信息和会员等级",
                Map.of(
                    "customer_id", Map.of("type", "string", "description", "客户ID，如 C-001")
                ),
                List.of("customer_id")
            ),
            buildTool(
                "create_ticket",
                "创建客服工单，用于需要人工跟进的问题",
                Map.of(
                    "title",       Map.of("type", "string", "description", "工单标题"),
                    "description", Map.of("type", "string", "description", "问题详细描述"),
                    "priority",    Map.of("type", "string", "description", "优先级：low / medium / high")
                ),
                List.of("title")
            )
        );

        return ResponseEntity.ok(Map.of("tools", tools));
    }

    /** 执行工具调用 */
    @PostMapping("/call")
    public ResponseEntity<Map<String, Object>> call(@RequestBody Map<String, Object> body) {
        String toolName = (String) body.get("tool_name");
        @SuppressWarnings("unchecked")
        Map<String, Object> arguments = (Map<String, Object>) body.getOrDefault("arguments", Map.of());

        if (toolName == null || toolName.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "tool_name 不能为空"));
        }

        try {
            String result = mcpService.call(toolName, arguments);
            return ResponseEntity.ok(Map.of("result", result));
        } catch (IllegalArgumentException e) {
            log.warn("MCP 调用参数错误: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            log.error("MCP 工具执行异常 tool={}", toolName, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "工具执行失败: " + e.getMessage()));
        }
    }

    // ------------------------------------------------------------------ //

    private Map<String, Object> buildTool(String name, String description,
                                          Map<String, Object> properties,
                                          List<String> required) {
        return Map.of(
            "name", name,
            "description", description,
            "parameters", Map.of(
                "type", "object",
                "properties", properties,
                "required", required
            )
        );
    }
}
