package com.intelliservice.backend.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TransferWaitingSessionResponse {
    private Long sessionId;
    private String username;
    private String contextType;
    private String contextId;
    private String lastMessageContent;
    private LocalDateTime waitingSince;
    private Long waitingSeconds;
}
