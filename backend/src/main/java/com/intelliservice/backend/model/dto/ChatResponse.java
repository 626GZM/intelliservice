package com.intelliservice.backend.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class ChatResponse {
    private String reply;
    private String agentName;
    private List<String> sources;
    private Integer tokenCount;
    private Integer responseTimeMs;
}
