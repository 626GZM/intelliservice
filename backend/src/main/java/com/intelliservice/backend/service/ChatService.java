package com.intelliservice.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intelliservice.backend.model.dto.AgentChatRequest;
import com.intelliservice.backend.model.dto.AgentChatResponse;
import com.intelliservice.backend.model.dto.ChatRequest;
import com.intelliservice.backend.model.dto.ChatResponse;
import com.intelliservice.backend.model.entity.Message;
import com.intelliservice.backend.monitor.MonitorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private static final String CACHE_KEY_PREFIX = "session:";
    private static final String CACHE_KEY_SUFFIX = ":messages";
    private static final int MAX_HISTORY = 20;   // Redis 缓存保留条数
    private static final int MAX_CONTEXT = 6;    // 传给 Agent 的上下文条数（3轮对话）
    private static final Duration CACHE_TTL = Duration.ofHours(24);

    private final MessageService messageService;
    private final SessionService sessionService;
    private final TitleGeneratorService titleGeneratorService;
    private final MonitorService monitorService;
    private final UserService userService;
    private final StringRedisTemplate stringRedisTemplate;
    private final WebClient agentWebClient;
    private final ObjectMapper objectMapper;

    public ChatResponse chat(ChatRequest request) {
        // 从 SecurityContext 中解析当前登录用户，无需 Controller 层传入
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Long userId = userService.getUserByUsername(auth.getName()).getId();
        Long sessionId = request.getSessionId();
        String userMessage = request.getMessage();
        String cacheKey = CACHE_KEY_PREFIX + sessionId + CACHE_KEY_SUFFIX;

        // 1. 读取会话信息（title 用于判断是否首条消息，context 用于透传给 Agent）
        var session = sessionService.getSession(sessionId);
        boolean isFirstMessage = "新对话".equals(session.getTitle());

        // 2. 从 Redis 取会话上下文，miss 则从数据库加载并回填
        List<Map<String, String>> history = loadHistory(cacheKey, sessionId);

        // 3. 保存用户消息到数据库
        messageService.saveMessage(sessionId, "user", userMessage, null, null, null);

        if ("waiting".equals(session.getTransferStatus()) || "serving".equals(session.getTransferStatus())) {
            history.add(Map.of("role", "user", "content", userMessage));
            if (history.size() > MAX_HISTORY) {
                history = history.subList(history.size() - MAX_HISTORY, history.size());
            }
            saveHistory(cacheKey, history);
            sessionService.touchSession(sessionId);
            if (isFirstMessage) {
                titleGeneratorService.generateAndUpdateTitle(sessionId, userMessage);
            }

            ChatResponse resp = new ChatResponse();
            resp.setAgentName("human_agent");
            resp.setTokenCount(0);
            resp.setResponseTimeMs(0);
            if ("waiting".equals(session.getTransferStatus())) {
                resp.setReply("正在为您转接人工客服，请稍候...");
            } else {
                resp.setReply(null);
            }
            return resp;
        }

        // 4. 调用 Python Agent 服务
        // history 只传最近 MAX_CONTEXT 条作为上下文，当前 message 单独传
        List<Map<String, String>> contextHistory = history.size() > MAX_CONTEXT
                ? history.subList(history.size() - MAX_CONTEXT, history.size())
                : new ArrayList<>(history);
        AgentChatRequest agentReq = new AgentChatRequest(sessionId, userId, userMessage, contextHistory,
                session.getContextType(), session.getContextId());
        AgentChatResponse agentResp;
        try {
            agentResp = agentWebClient.post()
                    .uri("/agent/chat")
                    .bodyValue(agentReq)
                    .retrieve()
                    .bodyToMono(AgentChatResponse.class)
                    .timeout(Duration.ofSeconds(60))
                    .block();
        } catch (WebClientResponseException e) {
            log.error("调用 Agent 失败 sessionId={} status={} body={}",
                    sessionId, e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new RuntimeException("AI 服务暂时不可用，请稍后重试");
        } catch (Exception e) {
            log.error("调用 Agent 失败 sessionId={} exceptionType={} message={}",
                    sessionId, e.getClass().getName(), e.getMessage(), e);
            throw new RuntimeException("AI 服务暂时不可用，请稍后重试");
        }

        if (agentResp == null) {
            throw new RuntimeException("系统繁忙，请稍后重试");
        }

        // 5. 保存助手回复到数据库
        Message assistantMsg = messageService.saveMessage(
                sessionId, "assistant", agentResp.getReply(),
                agentResp.getTokenCount(), agentResp.getResponseTimeMs(), agentResp.getAgentName()
        );

        // 6. 记录监控数据（写 agent_logs + 更新 Redis 统计）
        monitorService.recordCall(
                sessionId, assistantMsg.getId(),
                agentResp.getAgentName(), agentResp.getTokenCount(),
                agentResp.getResponseTimeMs(), "success"
        );

        // 7. 更新 Redis 缓存（保留最近 MAX_HISTORY 条）
        history.add(Map.of("role", "user", "content", userMessage));
        history.add(Map.of("role", "assistant", "content", agentResp.getReply()));
        if (history.size() > MAX_HISTORY) {
            history = history.subList(history.size() - MAX_HISTORY, history.size());
        }
        saveHistory(cacheKey, history);

        // 8. 更新会话 updatedAt
        sessionService.touchSession(sessionId);

        // 9. 异步生成标题（仅第一条消息触发，不阻塞返回）
        if (isFirstMessage) {
            titleGeneratorService.generateAndUpdateTitle(sessionId, userMessage);
        }

        // 10. 组装返回
        ChatResponse resp = new ChatResponse();
        resp.setReply(agentResp.getReply());
        resp.setAgentName(agentResp.getAgentName());
        resp.setSources(agentResp.getSources());
        resp.setTokenCount(agentResp.getTokenCount());
        resp.setResponseTimeMs(agentResp.getResponseTimeMs());
        return resp;
    }

    // ---- private helpers ------------------------------------------------

    private List<Map<String, String>> loadHistory(String cacheKey, Long sessionId) {
        String cached = stringRedisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached,
                        new TypeReference<List<Map<String, String>>>() {});
            } catch (Exception e) {
                log.warn("Redis history 反序列化失败，回退到数据库: {}", e.getMessage());
            }
        }

        // DB 回填：取最近 MAX_HISTORY 条消息
        List<Message> dbMessages = messageService.getSessionMessages(sessionId);
        List<Map<String, String>> history = dbMessages.stream()
                .map(m -> Map.of("role", m.getRole(), "content", m.getContent()))
                .collect(Collectors.toCollection(ArrayList::new));
        if (history.size() > MAX_HISTORY) {
            history = history.subList(history.size() - MAX_HISTORY, history.size());
        }

        saveHistory(cacheKey, history);
        return history;
    }

    private void saveHistory(String cacheKey, List<Map<String, String>> history) {
        try {
            String json = objectMapper.writeValueAsString(history);
            stringRedisTemplate.opsForValue().set(cacheKey, json, CACHE_TTL);
        } catch (Exception e) {
            log.warn("Redis history 写入失败: {}", e.getMessage());
        }
    }
}
