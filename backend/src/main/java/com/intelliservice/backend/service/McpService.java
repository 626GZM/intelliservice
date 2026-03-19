package com.intelliservice.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.intelliservice.backend.mapper.OrderMapper;
import com.intelliservice.backend.mapper.SessionMapper;
import com.intelliservice.backend.model.entity.Order;
import com.intelliservice.backend.model.entity.PenaltyRule;
import com.intelliservice.backend.model.entity.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class McpService {

    private final TicketService ticketService;
    private final ProductService productService;
    private final PenaltyRuleService penaltyRuleService;
    private final OrderMapper orderMapper;
    private final SessionMapper sessionMapper;
    private final ObjectMapper objectMapper;

    @Qualifier("deepseekWebClient")
    private final WebClient deepseekWebClient;

    // ------------------------------------------------------------------ //
    //  工具实现
    // ------------------------------------------------------------------ //

    /** 查询订单：优先查数据库，未找到返回提示 */
    public Map<String, Object> queryOrder(String orderId) {
        Order order = orderMapper.selectByOrderNo(orderId);
        if (order != null) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("order_no", order.getOrderNo());
            result.put("status", order.getStatus());
            result.put("quantity", order.getQuantity());
            result.put("total_amount", order.getTotalAmount());
            result.put("courier", order.getCourier());
            result.put("courier_no", order.getCourierNo());
            result.put("created_at", order.getCreatedAt() != null ? order.getCreatedAt().toString() : null);
            return result;
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("order_no", orderId);
        result.put("status", "未找到");
        result.put("message", "订单号 " + orderId + " 不存在，请核实后重试");
        return result;
    }

    /** 查询客户信息（模拟数据） */
    public Map<String, Object> queryCustomer(String customerId) {
        Map<String, Object> result = new LinkedHashMap<>();
        switch (customerId) {
            case "C-001" -> {
                result.put("customer_id", "C-001");
                result.put("name", "张三");
                result.put("level", "黄金会员");
                result.put("total_orders", 28);
                result.put("phone", "138****8001");
                result.put("registered_at", "2023-06-01");
            }
            case "C-002" -> {
                result.put("customer_id", "C-002");
                result.put("name", "李四");
                result.put("level", "钻石会员");
                result.put("total_orders", 156);
                result.put("phone", "139****9002");
                result.put("registered_at", "2022-01-15");
            }
            default -> {
                result.put("customer_id", customerId);
                result.put("status", "未找到");
                result.put("message", "客户 " + customerId + " 不存在");
            }
        }
        return result;
    }

    /** 创建工单 */
    public Map<String, Object> createTicket(Long userId, String title, String description, String priority) {
        log.info("create_ticket userId={} title={} priority={}", userId, title, priority);
        try {
            var ticket = ticketService.createTicket(userId, null, title, description, priority);
            log.info("create_ticket 成功 ticketId={} userId={}", ticket.getId(), userId);
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("ticket_id", ticket.getId());
            result.put("title", ticket.getTitle());
            result.put("priority", ticket.getPriority());
            result.put("status", "已创建");
            result.put("created_at", ticket.getCreatedAt().toString());
            return result;
        } catch (Exception e) {
            log.error("create_ticket 数据库插入失败 userId={} title={}: {}", userId, title, e.getMessage(), e);
            throw e;
        }
    }

    /** 查询商品详情 */
    public Map<String, Object> queryProduct(Long productId) {
        try {
            Product product = productService.getById(productId);
            return productToMap(product);
        } catch (RuntimeException e) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("error", e.getMessage());
            return result;
        }
    }

    /** 关键词搜索商品 */
    public List<Map<String, Object>> searchProducts(String keyword) {
        return productService.search(keyword).stream()
                .map(this::productToMap)
                .collect(Collectors.toList());
    }

    /** 推荐相似商品 */
    public List<Map<String, Object>> recommendSimilar(Long productId) {
        try {
            return productService.getSimilar(productId).stream()
                    .map(this::productToMap)
                    .collect(Collectors.toList());
        } catch (RuntimeException e) {
            return List.of(Map.of("error", e.getMessage()));
        }
    }

    /** 获取所有判罚规则 */
    public List<Map<String, Object>> getPenaltyRules() {
        return penaltyRuleService.listAll().stream().map(rule -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("rule_code", rule.getRuleCode());
            m.put("category", rule.getCategory());
            m.put("description", rule.getDescription());
            m.put("deduct_points", rule.getDeductPoints());
            m.put("fine_amount", rule.getFineAmount());
            m.put("severity", rule.getSeverity());
            return m;
        }).collect(Collectors.toList());
    }

    /**
     * AI 分析投诉并生成判罚建议，结果保存到工单 ai_suggestion。
     * 调用 DeepSeek 分析投诉与判罚规则的匹配度。
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> suggestPenalty(Long ticketId, String complaintDescription) {
        // 1. 构建规则文本
        List<PenaltyRule> rules = penaltyRuleService.listAll();
        StringBuilder rulesText = new StringBuilder();
        for (PenaltyRule r : rules) {
            rulesText.append(String.format("- %s（%s）：%s，扣分%d，罚款%.0f元，严重程度%s\n",
                    r.getRuleCode(), r.getCategory(), r.getDescription(),
                    r.getDeductPoints(), r.getFineAmount(), r.getSeverity()));
        }

        String systemPrompt = "你是电商平台判罚系统，根据买家投诉分析商户应受到的处罚。\n" +
                "严格按 JSON 格式返回，不要输出任何其他内容：\n" +
                "{\"applicable_rules\":[\"RULE_CODE\"],\"total_deduction\":0,\"total_fine\":0.0," +
                "\"reason\":\"...\",\"severity\":\"minor/moderate/severe\"}";

        String userPrompt = "判罚规则：\n" + rulesText +
                "\n投诉内容：" + complaintDescription +
                "\n请分析并以JSON格式返回建议处罚方案。";

        // 2. 调用 DeepSeek
        Map<String, Object> suggestion;
        try {
            Map<String, Object> requestBody = Map.of(
                    "model", "deepseek-chat",
                    "messages", List.of(
                            Map.of("role", "system", "content", systemPrompt),
                            Map.of("role", "user", "content", userPrompt)
                    ),
                    "response_format", Map.of("type", "json_object"),
                    "max_tokens", 256
            );

            Map<?, ?> response = deepseekWebClient.post()
                    .uri("/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();

            String content = extractContent(response);
            suggestion = objectMapper.readValue(content, Map.class);
        } catch (Exception e) {
            log.error("suggestPenalty DeepSeek调用失败 ticketId={}: {}", ticketId, e.getMessage());
            suggestion = new LinkedHashMap<>();
            suggestion.put("applicable_rules", List.of());
            suggestion.put("total_deduction", 0);
            suggestion.put("total_fine", 0.0);
            suggestion.put("reason", "AI分析失败，请人工判断");
            suggestion.put("severity", "minor");
        }

        // 3. 保存到工单
        try {
            String suggestionJson = objectMapper.writeValueAsString(suggestion);
            ticketService.saveAiSuggestion(ticketId, suggestionJson);
        } catch (Exception e) {
            log.error("保存AI建议失败 ticketId={}: {}", ticketId, e.getMessage());
        }

        return suggestion;
    }

    // ------------------------------------------------------------------ //
    //  工具分发入口
    // ------------------------------------------------------------------ //

    public String call(String toolName, Map<String, Object> arguments) throws Exception {
        return call(toolName, arguments, null, null);
    }

    public String call(String toolName, Map<String, Object> arguments,
                       Long ctxSessionId, Long ctxUserId) throws Exception {
        Object result = switch (toolName) {
            case "query_order" -> {
                String orderId = (String) arguments.get("order_id");
                if (orderId == null) throw new IllegalArgumentException("缺少参数: order_id");
                yield queryOrder(orderId);
            }
            case "query_customer" -> {
                String customerId = (String) arguments.get("customer_id");
                if (customerId == null) throw new IllegalArgumentException("缺少参数: customer_id");
                yield queryCustomer(customerId);
            }
            case "create_ticket" -> {
                Long userId = resolveUserId(ctxUserId, ctxSessionId);
                if (userId == null) {
                    log.error("create_ticket userId解析失败 ctxUserId={} ctxSessionId={} arguments={}",
                            ctxUserId, ctxSessionId, arguments);
                    throw new IllegalArgumentException("create_ticket 缺少 userId，请确保 MCP 请求体传入 userId 或 sessionId");
                }
                String title       = (String) arguments.getOrDefault("title", "Agent创建工单");
                String description = (String) arguments.getOrDefault("description", "");
                String priority    = (String) arguments.getOrDefault("priority", "medium");
                yield createTicket(userId, title, description, priority);
            }
            case "query_product" -> {
                Long productId = ((Number) arguments.get("product_id")).longValue();
                yield queryProduct(productId);
            }
            case "search_products" -> {
                String keyword = (String) arguments.get("keyword");
                if (keyword == null) throw new IllegalArgumentException("缺少参数: keyword");
                yield searchProducts(keyword);
            }
            case "recommend_similar" -> {
                Long productId = ((Number) arguments.get("product_id")).longValue();
                yield recommendSimilar(productId);
            }
            case "get_penalty_rules" -> getPenaltyRules();
            case "suggest_penalty" -> {
                Long ticketId = ((Number) arguments.get("ticket_id")).longValue();
                String desc = (String) arguments.getOrDefault("complaint_description", "");
                yield suggestPenalty(ticketId, desc);
            }
            default -> throw new IllegalArgumentException("未知工具: " + toolName);
        };

        return objectMapper.writeValueAsString(result);
    }

    // ---- helpers --------------------------------------------------------

    /**
     * 解析 userId，优先级：
     * 1. body 顶层 userId（Python Agent 从 chat 请求透传）
     * 2. 通过 sessionId 查 sessions 表（兜底）
     */
    private Long resolveUserId(Long ctxUserId, Long ctxSessionId) {
        if (ctxUserId != null && ctxUserId > 0) {
            log.debug("resolveUserId: userId={} (来自请求体)", ctxUserId);
            return ctxUserId;
        }
        if (ctxSessionId != null && ctxSessionId > 0) {
            var session = sessionMapper.selectById(ctxSessionId);
            if (session != null && session.getUserId() != null) {
                log.debug("resolveUserId: userId={} (通过 sessionId={} 查表)", session.getUserId(), ctxSessionId);
                return session.getUserId();
            }
            log.warn("resolveUserId: sessionId={} 在 sessions 表中不存在", ctxSessionId);
        }
        return null;
    }

    private Map<String, Object> productToMap(Product product) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", product.getId());
        m.put("name", product.getName());
        m.put("category", product.getCategory());
        m.put("price", product.getPrice());
        m.put("description", product.getDescription());
        m.put("stock", product.getStock());
        return m;
    }

    @SuppressWarnings("unchecked")
    private String extractContent(Map<?, ?> response) {
        if (response == null) return "{}";
        var choices = (List<Map<?, ?>>) response.get("choices");
        if (choices == null || choices.isEmpty()) return "{}";
        var message = (Map<?, ?>) choices.get(0).get("message");
        if (message == null) return "{}";
        String content = (String) message.get("content");
        return content != null ? content : "{}";
    }
}
