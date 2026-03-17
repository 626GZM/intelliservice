package com.intelliservice.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TitleGeneratorService {

    @Qualifier("deepseekWebClient")
    private final WebClient deepseekWebClient;
    private final SessionService sessionService;

    /**
     * 异步调用 DeepSeek 生成会话标题，完成后更新 sessions.title。
     * 不阻塞主对话流程。
     */
    @Async
    public void generateAndUpdateTitle(Long sessionId, String userMessage) {
        try {
            Map<String, Object> requestBody = Map.of(
                "model", "deepseek-chat",
                "messages", List.of(
                    Map.of("role", "system",
                           "content", "根据用户的问题，生成一个5-10个字的简短标题。只输出标题，不要引号和标点。"),
                    Map.of("role", "user", "content", userMessage)
                ),
                "max_tokens", 20
            );

            Map<?, ?> response = deepseekWebClient.post()
                    .uri("/chat/completions")
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            String title = extractTitle(response);
            if (title != null && !title.isBlank()) {
                sessionService.updateTitle(sessionId, title.trim());
                log.info("会话 {} 标题已更新: {}", sessionId, title.trim());
            }
        } catch (Exception e) {
            log.warn("生成会话标题失败，sessionId={}: {}", sessionId, e.getMessage());
            // 标题生成失败不影响正常对话，静默处理
        }
    }

    @SuppressWarnings("unchecked")
    private String extractTitle(Map<?, ?> response) {
        if (response == null) return null;
        var choices = (List<Map<?, ?>>) response.get("choices");
        if (choices == null || choices.isEmpty()) return null;
        var message = (Map<?, ?>) choices.get(0).get("message");
        if (message == null) return null;
        return (String) message.get("content");
    }
}
