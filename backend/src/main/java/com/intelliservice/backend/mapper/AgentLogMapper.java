package com.intelliservice.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.intelliservice.backend.model.entity.AgentLog;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface AgentLogMapper extends BaseMapper<AgentLog> {

    @Select("SELECT * FROM agent_logs WHERE session_id = #{sessionId} ORDER BY created_at DESC")
    List<AgentLog> selectBySessionIdOrderByCreatedAtDesc(Long sessionId);
}
