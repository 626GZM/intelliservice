package com.intelliservice.backend.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.intelliservice.backend.model.dto.AgentChatRequest;
import com.intelliservice.backend.model.dto.AgentChatResponse;
import com.intelliservice.backend.model.dto.ChatRequest;
import com.intelliservice.backend.model.dto.ChatResponse;
import com.intelliservice.backend.model.entity.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

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
    private final StringRedisTemplate stringRedisTemplate;
    private final WebClient agentWebClient;
    private final ObjectMapper objectMapper;

    public ChatResponse chat(Long userId, ChatRequest request) {
        Long sessionId = request.getSessionId();
        String userMessage = request.getMessage();
        String cacheKey = CACHE_KEY_PREFIX + sessionId + CACHE_KEY_SUFFIX;

        // 1. 判断是否是第一条消息（title 还是"新对话"），在发消息前检查，避免并发重复生成
        boolean isFirstMessage = "新对话".equals(sessionService.getSession(sessionId).getTitle());

        // 2. 从 Redis 取会话上下文，miss 则从数据库加载并回填
        List<Map<String, String>> history = loadHistory(cacheKey, sessionId);

        // 3. 保存用户消息到数据库
        messageService.saveMessage(sessionId, "user", userMessage, null, null, null);

        // 4. 调用 Python Agent 服务
        // history 只传最近 MAX_CONTEXT 条作为上下文，当前 message 单独传
        List<Map<String, String>> contextHistory = history.size() > MAX_CONTEXT
                ? history.subList(history.size() - MAX_CONTEXT, history.size())
                : new ArrayList<>(history);
        AgentChatRequest agentReq = new AgentChatRequest(sessionId, userMessage, contextHistory);
        AgentChatResponse agentResp = agentWebClient.post()
                .uri("/agent/chat")
                .bodyValue(agentReq)
                .retrieve()
                .bodyToMono(AgentChatResponse.class)
                .block();

        if (agentResp == null) {
            throw new RuntimeException("Agent 服务无响应");
        }

        // 5. 保存助手回复到数据库
        messageService.saveMessage(
                sessionId, "assistant", agentResp.getReply(),
                agentResp.getTokenCount(), agentResp.getResponseTimeMs(), agentResp.getAgentName()
        );

        // 6. 更新 Redis 缓存（保留最近 MAX_HISTORY 条）
        history.add(Map.of("role", "user", "content", userMessage));
        history.add(Map.of("role", "assistant", "content", agentResp.getReply()));
        if (history.size() > MAX_HISTORY) {
            history = history.subList(history.size() - MAX_HISTORY, history.size());
        }
        saveHistory(cacheKey, history);

        // 7. 更新会话 updatedAt
        sessionService.touchSession(sessionId);

        // 8. 异步生成标题（仅第一条消息触发，不阻塞返回）
        if (isFirstMessage) {
            titleGeneratorService.generateAndUpdateTitle(sessionId, userMessage);
        }

        // 9. 组装返回
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
