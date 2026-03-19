package com.intelliservice.backend.service;

import com.intelliservice.backend.mapper.TicketMapper;
import com.intelliservice.backend.model.dto.ReviewTicketRequest;
import com.intelliservice.backend.model.entity.Ticket;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketMapper ticketMapper;
    private final MerchantService merchantService;
    private final ObjectMapper objectMapper;

    public Ticket createTicket(Long userId, Long sessionId, String title,
                               String description, String priority) {
        Ticket ticket = new Ticket();
        ticket.setUserId(userId);
        ticket.setSessionId(sessionId);
        ticket.setTitle(title);
        ticket.setDescription(description);
        ticket.setPriority(priority != null ? priority : "medium");
        ticket.setStatus("open");
        ticket.setReviewStatus("pending");
        ticket.setCreatedAt(LocalDateTime.now());
        ticketMapper.insert(ticket);
        return ticket;
    }

    public List<Ticket> getUserTickets(Long userId) {
        return ticketMapper.selectByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Ticket> listAll(String status) {
        if (status != null && !status.isBlank()) {
            return ticketMapper.selectByStatusOrderByCreatedAtDesc(status);
        }
        return ticketMapper.selectAllOrderByCreatedAtDesc();
    }

    public Ticket getTicketById(Long ticketId) {
        Ticket ticket = ticketMapper.selectById(ticketId);
        if (ticket == null) {
            throw new RuntimeException("工单不存在");
        }
        return ticket;
    }

    public Ticket updateTicketStatus(Long ticketId, String status) {
        Ticket ticket = getTicketById(ticketId);
        ticket.setStatus(status);
        if ("resolved".equals(status)) {
            ticket.setResolvedAt(LocalDateTime.now());
        }
        ticketMapper.updateById(ticket);
        return ticket;
    }

    /** Python Agent 调用：保存 AI 建议的处罚方案 */
    public Ticket saveAiSuggestion(Long ticketId, String aiSuggestionJson) {
        Ticket ticket = getTicketById(ticketId);
        ticket.setAiSuggestion(aiSuggestionJson);
        ticket.setReviewStatus("ai_suggested");
        ticket.setStatus("processing");
        ticketMapper.updateById(ticket);
        return ticket;
    }

    /** 客服审阅：通过或驳回，并填写最终处罚方案 */
    public Ticket review(Long ticketId, Long reviewerId, ReviewTicketRequest req) {
        Ticket ticket = getTicketById(ticketId);
        if (Boolean.TRUE.equals(req.getApproved())) {
            ticket.setReviewStatus("reviewed");
            if (req.getFinalPenalty() != null) {
                ticket.setFinalPenalty(writeJson(req.getFinalPenalty()));
            } else {
                ticket.setFinalPenalty(ticket.getAiSuggestion());
            }
        } else {
            ticket.setReviewStatus("rejected");
        }
        ticket.setReviewedBy(reviewerId);
        ticket.setReviewedAt(LocalDateTime.now());
        ticketMapper.updateById(ticket);
        return ticket;
    }

    /** 执行处罚：对商户扣分+罚款，更新 review_status=executed */
    @SuppressWarnings("unchecked")
    public Ticket executePenalty(Long ticketId) {
        Ticket ticket = getTicketById(ticketId);
        if (!"reviewed".equals(ticket.getReviewStatus())) {
            throw new RuntimeException("工单尚未通过审阅，无法执行处罚");
        }
        if (ticket.getMerchantId() == null) {
            throw new RuntimeException("工单未关联商户，无法执行处罚");
        }

        String penaltyJson = ticket.getFinalPenalty() != null
                ? ticket.getFinalPenalty() : ticket.getAiSuggestion();
        if (penaltyJson == null) {
            throw new RuntimeException("工单没有处罚方案");
        }

        try {
            Map<String, Object> penalty = objectMapper.readValue(penaltyJson, Map.class);
            int deductPoints = readInt(penalty, "deductPoints", "total_deduction", "deduct_points");
            double fineAmount = readDouble(penalty, "fine", "total_fine", "fineAmount", "fine_amount");
            merchantService.applyPenalty(ticket.getMerchantId(), deductPoints, fineAmount);
        } catch (Exception e) {
            log.error("解析处罚方案失败 ticketId={}: {}", ticketId, e.getMessage());
            throw new RuntimeException("处罚方案格式错误，执行失败");
        }

        ticket.setReviewStatus("executed");
        ticketMapper.updateById(ticket);
        return ticket;
    }

    /** 结算工单：状态改为 closed */
    public Ticket settle(Long ticketId) {
        Ticket ticket = getTicketById(ticketId);
        ticket.setStatus("closed");
        ticketMapper.updateById(ticket);
        return ticket;
    }

    public String toJson(Object value) {
        return writeJson(value);
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new RuntimeException("处罚方案格式错误");
        }
    }

    private int readInt(Map<String, Object> payload, String... keys) {
        for (String key : keys) {
            Object value = payload.get(key);
            if (value instanceof Number number) {
                return number.intValue();
            }
        }
        return 0;
    }

    private double readDouble(Map<String, Object> payload, String... keys) {
        for (String key : keys) {
            Object value = payload.get(key);
            if (value instanceof Number number) {
                return number.doubleValue();
            }
        }
        return 0.0;
    }
}
