package com.intelliservice.backend.service;

import com.intelliservice.backend.model.dto.TransferWaitingSessionResponse;
import com.intelliservice.backend.model.dto.TransferServingSessionResponse;
import com.intelliservice.backend.mapper.SessionMapper;
import com.intelliservice.backend.model.dto.CreateSessionRequest;
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

    public Session createSession(Long userId, CreateSessionRequest req) {
        Session session = new Session();
        session.setUserId(userId);
        session.setTitle("新对话");
        session.setStatus("active");
        session.setTransferStatus("ai");
        session.setCreatedAt(LocalDateTime.now());
        session.setUpdatedAt(LocalDateTime.now());

        if (req != null) {
            if (req.getContextType() != null) session.setContextType(req.getContextType());
            if (req.getContextId() != null) session.setContextId(req.getContextId());
        }
        if (session.getContextType() == null) session.setContextType("general");

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

    public Session getSession(Long sessionId) {
        Session session = sessionMapper.selectById(sessionId);
        if (session == null) {
            throw new RuntimeException("会话不存在");
        }
        return session;
    }

    public void updateTitle(Long sessionId, String title) {
        Session session = new Session();
        session.setId(sessionId);
        session.setTitle(title);
        sessionMapper.updateById(session);
    }

    public void touchSession(Long sessionId) {
        Session session = new Session();
        session.setId(sessionId);
        session.setUpdatedAt(LocalDateTime.now());
        sessionMapper.updateById(session);
    }

    // ---- 转人工 ----

    /** 买家请求转人工 */
    public Session requestTransfer(Long sessionId, Long userId) {
        Session session = getSession(sessionId);
        if (!session.getUserId().equals(userId)) {
            throw new RuntimeException("无权操作该会话");
        }
        if ("serving".equals(session.getTransferStatus())) {
            throw new RuntimeException("该会话已有客服接入");
        }
        Session update = new Session();
        update.setId(sessionId);
        update.setTransferStatus("waiting");
        update.setUpdatedAt(LocalDateTime.now());
        sessionMapper.updateById(update);
        return getSession(sessionId);
    }

    /** 获取等待接入的会话列表 */
    public List<TransferWaitingSessionResponse> getWaitingSessions() {
        return sessionMapper.selectWaitingSessionSummaries();
    }

    /** 获取当前客服正在服务的会话列表 */
    public List<TransferServingSessionResponse> getServingSessions(Long agentId) {
        return sessionMapper.selectServingSessionSummaries(agentId);
    }

    /** 客服接入会话 */
    public Session acceptTransfer(Long sessionId, Long agentId) {
        Session session = getSession(sessionId);
        if (!"waiting".equals(session.getTransferStatus())) {
            throw new RuntimeException("该会话不在等待状态");
        }
        Session update = new Session();
        update.setId(sessionId);
        update.setTransferStatus("serving");
        update.setAgentId(agentId);
        update.setUpdatedAt(LocalDateTime.now());
        sessionMapper.updateById(update);
        return getSession(sessionId);
    }

    /** 客服人工回复 */
    public Session replyAsHuman(Long sessionId, Long agentId, boolean isAdmin, String message) {
        Session session = getSession(sessionId);
        if (!"serving".equals(session.getTransferStatus())) {
            throw new RuntimeException("该会话当前未处于人工服务中");
        }
        if (session.getAgentId() == null) {
            throw new RuntimeException("该会话尚未分配客服");
        }
        if (!isAdmin && !session.getAgentId().equals(agentId)) {
            throw new RuntimeException("当前客服无权回复该会话");
        }
        messageService.saveMessage(sessionId, "assistant", message, 0, 0, "human_agent");
        touchSession(sessionId);
        return getSession(sessionId);
    }

    /** 客服关闭人工会话 */
    public Session closeTransfer(Long sessionId, Long agentId, boolean isAdmin) {
        Session session = getSession(sessionId);
        if ("ai".equals(session.getTransferStatus())) {
            throw new RuntimeException("该会话未转人工");
        }
        if (!isAdmin && session.getAgentId() != null && !session.getAgentId().equals(agentId)) {
            throw new RuntimeException("当前客服无权关闭该会话");
        }
        Session update = new Session();
        update.setId(sessionId);
        update.setTransferStatus("closed");
        update.setUpdatedAt(LocalDateTime.now());
        sessionMapper.updateById(update);
        return getSession(sessionId);
    }
}
