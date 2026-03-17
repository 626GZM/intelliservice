package com.intelliservice.backend.service;

import com.intelliservice.backend.mapper.SessionMapper;
import com.intelliservice.backend.model.dto.SessionDetailResponse;
import com.intelliservice.backend.model.entity.Session;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionMapper sessionMapper;
    private final MessageService messageService;

    public Session createSession(Long userId) {
        Session session = new Session();
        session.setUserId(userId);
        session.setTitle("新对话");
        session.setStatus("active");
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());
        sessionMapper.insert(session);
        return session;
    }

    public List<Session> getUserSessions(Long userId) {
        return sessionMapper.selectByUserIdOrderByUpdatedAtDesc(userId);
    }

    public SessionDetailResponse getSessionDetail(Long sessionId) {
        Session session = sessionMapper.selectById(sessionId);
        if (session == null) {
            throw new RuntimeException("会话不存在");
        }
        SessionDetailResponse detail = new SessionDetailResponse();
        detail.setSession(session);
        detail.setMessages(messageService.getSessionMessages(sessionId));
        return detail;
    }

    public void deleteSession(Long sessionId) {
        sessionMapper.deleteById(sessionId);
    }

    /** 更新会话最后活跃时间，在 ChatService 中调用 */
    public void touchSession(Long sessionId) {
        Session session = new Session();
        session.setId(sessionId);
        session.setUpdatedAt(LocalDateTime.now());
        sessionMapper.updateById(session);
    }
}
