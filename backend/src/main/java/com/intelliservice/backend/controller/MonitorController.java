package com.intelliservice.backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.intelliservice.backend.mapper.AgentLogMapper;
import com.intelliservice.backend.mapper.SessionMapper;
import com.intelliservice.backend.model.dto.ApiResponse;
import com.intelliservice.backend.model.entity.AgentLog;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/monitor")
@RequiredArgsConstructor
public class MonitorController {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final StringRedisTemplate stringRedisTemplate;
    private final AgentLogMapper agentLogMapper;
    private final SessionMapper sessionMapper;

    /** 今日概览 */
    @GetMapping("/overview")
    public ApiResponse<Map<String, Object>> overview() {
        String today = LocalDate.now().format(DATE_FMT);
        Map<Object, Object> daily = stringRedisTemplate.opsForHash()
                .entries("stats:daily:" + today);

        long totalRequests  = parseLong(daily.get("total_requests"));
        long totalTokens    = parseLong(daily.get("total_tokens"));
        long totalTimeMs    = parseLong(daily.get("total_time_ms"));
        long avgResponseTime = totalRequests > 0 ? totalTimeMs / totalRequests : 0;
        int activeUsers      = sessionMapper.countActiveTodayUsers();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("todayRequests",   totalRequests);
        result.put("todayTokens",     totalTokens);
        result.put("avgResponseTime", avgResponseTime);
        result.put("activeUsers",     activeUsers);
        return ApiResponse.success(result);
    }

    /** 各 Agent 调用统计（优先从 DB 聚合，保证历史数据完整） */
    @GetMapping("/agents")
    public ApiResponse<List<Map<String, Object>>> agents() {
        List<Map<String, Object>> stats = agentLogMapper.selectAgentStats();
        return ApiResponse.success(stats);
    }

    /** 最近 N 天 Token 消耗趋势（从 Redis 读取） */
    @GetMapping("/tokens/daily")
    public ApiResponse<List<Map<String, Object>>> tokenDaily(
            @RequestParam(defaultValue = "7") int days) {

        List<Map<String, Object>> trend = new ArrayList<>();
        for (int i = days - 1; i >= 0; i--) {
            String date = LocalDate.now().minusDays(i).format(DATE_FMT);
            Map<Object, Object> raw = stringRedisTemplate.opsForHash()
                    .entries("stats:daily:" + date);

            long requests = parseLong(raw.get("total_requests"));
            long tokens   = parseLong(raw.get("total_tokens"));
            long timeMs   = parseLong(raw.get("total_time_ms"));
            long avgTime  = requests > 0 ? timeMs / requests : 0;

            Map<String, Object> day = new LinkedHashMap<>();
            day.put("date",     date);
            day.put("requests", requests);
            day.put("tokens",   tokens);
            day.put("avgTime",  avgTime);
            trend.add(day);
        }
        return ApiResponse.success(trend);
    }

    /** 调用日志分页（按时间倒序） */
    @GetMapping("/logs")
    public ApiResponse<Map<String, Object>> logs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<AgentLog> pageReq = new Page<>(page + 1, size); // MyBatis-Plus 页码从 1 开始
        Page<AgentLog> pageResult = agentLogMapper.selectPage(
                pageReq,
                new QueryWrapper<AgentLog>().orderByDesc("created_at")
        );

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("content",       pageResult.getRecords());
        result.put("totalElements", pageResult.getTotal());
        result.put("totalPages",    pageResult.getPages());
        result.put("page",          page);
        result.put("size",          size);
        return ApiResponse.success(result);
    }

    // ------------------------------------------------------------------ //

    private long parseLong(Object val) {
        if (val == null) return 0L;
        try {
            return Long.parseLong(val.toString());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }
}
