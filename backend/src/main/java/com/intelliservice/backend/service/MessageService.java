package com.intelliservice.backend.service;

import com.intelliservice.backend.mapper.MessageMapper;
import com.intelliservice.backend.model.entity.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageMapper messageMapper;

    public Message saveMessage(Long sessionId, String role, String content,
                               Integer tokenCount, Integer responseTimeMs, String agentName) {
        Message msg = new Message();
        msg.setSessionId(sessionId);
        msg.setRole(role);
        msg.setContent(content);
        msg.setTokenCount(tokenCount != null ? tokenCount : 0);
        msg.setResponseTimeMs(responseTimeMs != null ? responseTimeMs : 0);
        msg.setAgentName(agentName);
        msg.setCreatedAt(LocalDateTime.now());
        messageMapper.insert(msg);
        return msg;
    }

    public List<Message> getSessionMessages(Long sessionId) {
        return messageMapper.selectBySessionIdOrderByCreatedAtAsc(sessionId);
    }
}
