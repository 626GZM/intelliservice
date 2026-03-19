package com.intelliservice.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.intelliservice.backend.model.entity.AgentLog;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

public interface AgentLogMapper extends BaseMapper<AgentLog> {

    @Select("SELECT * FROM agent_logs WHERE session_id = #{sessionId} ORDER BY created_at DESC")
    List<AgentLog> selectBySessionIdOrderByCreatedAtDesc(Long sessionId);

    @Select("SELECT agent_name AS name, COUNT(*) AS callCount, " +
            "COALESCE(SUM(completion_tokens), 0) AS totalTokens " +
            "FROM agent_logs WHERE agent_name IS NOT NULL " +
            "GROUP BY agent_name ORDER BY callCount DESC")
    List<Map<String, Object>> selectAgentStats();
}
