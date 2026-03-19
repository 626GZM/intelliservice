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
            buildTool("query_order",
                "根据订单号查询订单状态和物流信息",
                Map.of("order_id", prop("string", "订单号，如 ORD-88892")),
                List.of("order_id")),
            buildTool("query_customer",
                "根据客户ID查询客户基本信息和会员等级",
                Map.of("customer_id", prop("string", "客户ID，如 C-001")),
                List.of("customer_id")),
            buildTool("create_ticket",
                "创建客服工单，用于需要人工跟进的问题",
                Map.of(
                    "user_id",     prop("integer", "发起对话的用户ID，由系统自动填入"),
                    "title",       prop("string",  "工单标题"),
                    "description", prop("string",  "问题详细描述"),
                    "priority",    prop("string",  "优先级：low / medium / high")),
                List.of("user_id", "title")),
            buildTool("query_product",
                "根据商品ID查询商品详情，包括名称、价格、库存",
                Map.of("product_id", prop("integer", "商品ID")),
                List.of("product_id")),
            buildTool("search_products",
                "根据关键词搜索商品，返回匹配的商品列表",
                Map.of("keyword", prop("string", "搜索关键词，如手机壳、数据线")),
                List.of("keyword")),
            buildTool("recommend_similar",
                "根据商品ID推荐同类商品，最多返回5个",
                Map.of("product_id", prop("integer", "商品ID")),
                List.of("product_id")),
            buildTool("get_penalty_rules",
                "获取所有商户判罚规则列表，包含扣分和罚款标准",
                Map.of(),
                List.of()),
            buildTool("suggest_penalty",
                "根据投诉内容AI分析并生成处罚建议，结果自动保存到工单",
                Map.of(
                    "ticket_id",             prop("integer", "工单ID"),
                    "complaint_description", prop("string",  "投诉内容详述")),
                List.of("ticket_id", "complaint_description"))
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
            return ResponseEntity.badRequest().body(Map.of("error", "tool_name 不能为空"));
        }

        // userId 可选，Python Agent 透传（camelCase 和 snake_case 均接受）
        Number userIdNum = body.containsKey("userId")
                ? (Number) body.get("userId")
                : body.containsKey("user_id") ? (Number) body.get("user_id") : null;
        Long userId = (userIdNum != null && userIdNum.longValue() > 0) ? userIdNum.longValue() : null;

        // sessionId 可选（camelCase 和 snake_case 均接受）
        Number sessionIdNum = body.containsKey("sessionId")
                ? (Number) body.get("sessionId")
                : body.containsKey("session_id") ? (Number) body.get("session_id") : null;
        Long sessionId = sessionIdNum != null ? sessionIdNum.longValue() : null;

        try {
            String result = mcpService.call(toolName, arguments, sessionId, userId);
            return ResponseEntity.ok(Map.of("result", result));
        } catch (IllegalArgumentException e) {
            log.warn("MCP 调用参数错误: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
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
                "required", required));
    }

    private Map<String, Object> prop(String type, String description) {
        return Map.of("type", type, "description", description);
    }
}
