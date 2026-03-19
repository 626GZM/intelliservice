package com.intelliservice.backend.model.dto;

import lombok.Data;

@Data
public class TransferReplyRequest {
    private Long sessionId;
    private String message;
}
