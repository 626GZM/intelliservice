package com.intelliservice.backend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tool_configs")
public class ToolConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 工具标识，如 query_order */
    private String name;

    /** 显示名称 */
    private String displayName;

    /** 工具描述（LLM 看到的） */
    private String description;

    /** 工具类型: mcp / http / rag */
    private String toolType;

    /** 工具配置（JSON 字符串） */
    private String config;

    /** 是否启用（1=启用，0=禁用） */
    private Integer enabled;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
