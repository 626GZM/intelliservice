package com.intelliservice.backend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("agent_logs")
public class AgentLog {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long sessionId;

    private Long messageId;

    private String agentName;

    private String toolName;

    /** JSON 类型，以字符串存储 */
    private String toolInput;

    /** JSON 类型，以字符串存储 */
    private String toolOutput;

    private Integer promptTokens;

    private Integer completionTokens;

    private Integer responseTimeMs;

    /** 对应 ENUM('success','error','timeout') */
    private String status;

    private String errorMessage;

    private LocalDateTime createdAt;
}
