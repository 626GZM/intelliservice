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

    private String message;

    /** 最近 20 条历史，每条含 role / content 字段 */
    private List<Map<String, String>> history;
}
