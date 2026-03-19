package com.intelliservice.backend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.intelliservice.backend.model.entity.ToolConfig;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ToolConfigMapper extends BaseMapper<ToolConfig> {

    @Select("SELECT * FROM tool_configs WHERE enabled = 1 ORDER BY id ASC")
    List<ToolConfig> selectEnabled();

    @Select("SELECT * FROM tool_configs WHERE name = #{name} LIMIT 1")
    ToolConfig selectByName(String name);

    @Select("SELECT * FROM tool_configs ORDER BY id ASC")
    List<ToolConfig> selectAllOrdered();
}
