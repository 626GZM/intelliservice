package com.intelliservice.backend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("agent_configs")
public class AgentConfig {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** Agent 标识，如 knowledge_agent */
    private String name;

    /** 显示名称 */
    private String displayName;

    /** 描述 */
    private String description;

    /** System Prompt */
    private String systemPrompt;

    /** Agent 类型: knowledge / order / ticket / custom */
    private String agentType;

    /** 可用工具列表（JSON 字符串，如 ["search_knowledge"]） */
    private String tools;

    /** 使用的 LLM 模型 */
    private String model;

    /** 温度参数 */
    private Double temperature;

    /** 是否启用（1=启用，0=禁用） */
    private Integer enabled;

    /** 路由优先级（越小越优先） */
    private Integer sortOrder;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
