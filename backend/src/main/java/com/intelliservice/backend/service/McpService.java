package com.intelliservice.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class McpService {

    private final TicketService ticketService;
    private final ObjectMapper objectMapper;

    // ------------------------------------------------------------------ //
    //  工具实现
    // ------------------------------------------------------------------ //

    /** 查询订单：先查数据库，未实现订单表则返回模拟数据 */
    public Map<String, Object> queryOrder(String orderId) {
        // 实际项目中替换为真实 OrderMapper 查询
        Map<String, Object> result = new LinkedHashMap<>();
        switch (orderId) {
            case "ORD-88892" -> {
                result.put("order_id", "ORD-88892");
                result.put("status", "运输中");
                result.put("courier", "顺丰");
                result.put("courier_no", "SF1234567890");
                result.put("estimated_delivery", "明天下午");
            }
            case "ORD-66543" -> {
                result.put("order_id", "ORD-66543");
                result.put("status", "已签收");
                result.put("courier", "顺丰");
                result.put("courier_no", "SF0987654321");
                result.put("signed_at", "2026-03-15 14:32:00");
            }
            default -> {
                result.put("order_id", orderId);
                result.put("status", "未找到");
                result.put("message", "订单号 " + orderId + " 不存在");
            }
        }
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

    /** 创建工单，返回工单信息 */
    public Map<String, Object> createTicket(String title, String description, String priority) {
        // userId=0 表示由系统/Agent创建
        var ticket = ticketService.createTicket(0L, null, title, description, priority);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("ticket_id", ticket.getId());
        result.put("title", ticket.getTitle());
        result.put("priority", ticket.getPriority());
        result.put("status", ticket.getStatus());
        result.put("created_at", ticket.getCreatedAt().toString());
        return result;
    }

    // ------------------------------------------------------------------ //
    //  工具分发入口
    // ------------------------------------------------------------------ //

    public String call(String toolName, Map<String, Object> arguments) throws Exception {
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
                String title       = (String) arguments.getOrDefault("title", "Agent创建工单");
                String description = (String) arguments.getOrDefault("description", "");
                String priority    = (String) arguments.getOrDefault("priority", "medium");
                yield createTicket(title, description, priority);
            }
            default -> throw new IllegalArgumentException("未知工具: " + toolName);
        };

        return objectMapper.writeValueAsString(result);
    }
}
