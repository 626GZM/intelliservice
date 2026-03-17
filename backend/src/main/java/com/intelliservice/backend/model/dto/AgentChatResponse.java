package com.intelliservice.backend.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class AgentChatResponse {

    private String reply;

    @JsonProperty("agent_name")
    private String agentName;

    private List<String> sources;

    @JsonProperty("token_count")
    private Integer tokenCount;

    @JsonProperty("response_time_ms")
    private Integer responseTimeMs;
}
