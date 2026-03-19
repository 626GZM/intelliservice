package com.intelliservice.backend.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class AgentChatRequest {

    @JsonProperty("session_id")
    private Long sessionId;

    /** 当前发送消息的用户 ID，Python Agent 调用 create_ticket 时需透传 */
    @JsonProperty("user_id")
    private Long userId;

    private String message;

    /** 最近若干条历史，每条含 role / content 字段 */
    private List<Map<String, String>> history;

    /** 对话上下文类型：general / product / order */
    @JsonProperty("context_type")
    private String contextType;

    /** 关联的 productId 或 orderNo */
    @JsonProperty("context_id")
    private String contextId;
}
