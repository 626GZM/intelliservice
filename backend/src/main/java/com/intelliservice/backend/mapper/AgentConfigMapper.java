package com.intelliservice.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.intelliservice.backend.model.entity.AgentConfig;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface AgentConfigMapper extends BaseMapper<AgentConfig> {

    @Select("SELECT * FROM agent_configs WHERE enabled = 1 ORDER BY sort_order ASC")
    List<AgentConfig> selectEnabled();

    @Select("SELECT * FROM agent_configs WHERE name = #{name} LIMIT 1")
    AgentConfig selectByName(String name);

    @Select("SELECT * FROM agent_configs ORDER BY sort_order ASC")
    List<AgentConfig> selectAllOrdered();
}
