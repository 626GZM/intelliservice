package com.intelliservice.backend.service;

import com.intelliservice.backend.mapper.TicketMapper;
import com.intelliservice.backend.model.entity.Ticket;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketMapper ticketMapper;

    public Ticket createTicket(Long userId, Long sessionId, String title,
                               String description, String priority) {
        Ticket ticket = new Ticket();
        ticket.setUserId(userId);
        ticket.setSessionId(sessionId);
        ticket.setTitle(title);
        ticket.setDescription(description);
        ticket.setPriority(priority != null ? priority : "medium");
        ticket.setStatus("open");
        ticket.setCreatedAt(LocalDateTime.now());
        ticketMapper.insert(ticket);
        return ticket;
    }

    public List<Ticket> getUserTickets(Long userId) {
        return ticketMapper.selectByUserIdOrderByCreatedAtDesc(userId);
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
}
