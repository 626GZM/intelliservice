package com.intelliservice.backend.monitor;

import com.intelliservice.backend.model.dto.ChatRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 拦截 ChatService.chat()，记录调用的起止时间与总耗时。
 * 实际 agent_logs 写入 + Redis 统计由 MonitorService 负责（在 ChatService 内调用）。
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AgentCallAspect {

    private static final DateTimeFormatter TS_FMT = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

    @Around("execution(* com.intelliservice.backend.service.ChatService.chat(..))")
    public Object aroundChat(ProceedingJoinPoint pjp) throws Throwable {
        LocalDateTime startTime = LocalDateTime.now();
        long startMs = System.currentTimeMillis();

        // 提取 sessionId 用于日志输出
        Object[] args = pjp.getArgs();
        Long sessionId = null;
        for (Object arg : args) {
            if (arg instanceof ChatRequest req) {
                sessionId = req.getSessionId();
                break;
            }
        }

        log.info("[AOP] Chat start  sessionId={} at {}", sessionId, startTime.format(TS_FMT));

        try {
            Object result = pjp.proceed();
            long elapsed = System.currentTimeMillis() - startMs;
            log.info("[AOP] Chat finish sessionId={} elapsed={}ms at {}",
                    sessionId, elapsed, LocalDateTime.now().format(TS_FMT));
            return result;
        } catch (Throwable ex) {
            long elapsed = System.currentTimeMillis() - startMs;
            log.error("[AOP] Chat error  sessionId={} elapsed={}ms error={}",
                    sessionId, elapsed, ex.getMessage());
            throw ex;
        }
    }
}
