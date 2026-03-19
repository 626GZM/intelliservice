package com.intelliservice.backend.monitor;

import com.intelliservice.backend.mapper.AgentLogMapper;
import com.intelliservice.backend.model.entity.AgentLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class MonitorService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final AgentLogMapper agentLogMapper;
    private final StringRedisTemplate stringRedisTemplate;

    /**
     * 写 agent_logs 表 + 更新 Redis 统计。
     *
     * @param sessionId      会话 ID
     * @param messageId      助手消息 ID（可为 null）
     * @param agentName      Agent 名称
     * @param tokenCount     本次消耗 token 数
     * @param responseTimeMs Python Agent 响应耗时
     * @param status         success / error / timeout
     */
    public void recordCall(Long sessionId, Long messageId,
                           String agentName, Integer tokenCount,
                           Integer responseTimeMs, String status) {
        // 1. 写 agent_logs
        AgentLog agentLog = new AgentLog();
        agentLog.setSessionId(sessionId);
        agentLog.setMessageId(messageId);
        agentLog.setAgentName(agentName);
        agentLog.setCompletionTokens(tokenCount != null ? tokenCount : 0);
        agentLog.setPromptTokens(0);
        agentLog.setResponseTimeMs(responseTimeMs != null ? responseTimeMs : 0);
        agentLog.setStatus(status);
        agentLog.setCreatedAt(LocalDateTime.now());
        try {
            agentLogMapper.insert(agentLog);
        } catch (Exception e) {
            log.warn("写入 agent_logs 失败: {}", e.getMessage());
        }

        // 2. 更新 Redis 统计
        try {
            updateRedisStats(agentName, tokenCount, responseTimeMs);
        } catch (Exception e) {
            log.warn("更新 Redis 监控统计失败: {}", e.getMessage());
        }
    }

    // ------------------------------------------------------------------ //

    private void updateRedisStats(String agentName, Integer tokenCount, Integer responseTimeMs) {
        String today = LocalDate.now().format(DATE_FMT);
        String dailyKey = "stats:daily:" + today;
        int tokens = tokenCount != null ? tokenCount : 0;
        int timeMs = responseTimeMs != null ? responseTimeMs : 0;

        // 按天统计
        var dailyOps = stringRedisTemplate.opsForHash();
        dailyOps.increment(dailyKey, "total_requests", 1);
        dailyOps.increment(dailyKey, "total_tokens", tokens);
        dailyOps.increment(dailyKey, "total_time_ms", timeMs);
        stringRedisTemplate.expire(dailyKey, 30, TimeUnit.DAYS);

        // 按 Agent 统计
        if (agentName != null && !agentName.isBlank()) {
            String agentKey = "stats:agent:" + agentName;
            var agentOps = stringRedisTemplate.opsForHash();
            agentOps.increment(agentKey, "call_count", 1);
            agentOps.increment(agentKey, "total_tokens", tokens);
            stringRedisTemplate.expire(agentKey, 30, TimeUnit.DAYS);
            // 维护所有 agentName 的集合，供 /agents 接口遍历
            stringRedisTemplate.opsForSet().add("stats:agents", agentName);
        }
    }
}
