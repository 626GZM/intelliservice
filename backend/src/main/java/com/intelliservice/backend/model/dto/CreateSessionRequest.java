package com.intelliservice.backend.model.dto;

import lombok.Data;

@Data
public class CreateSessionRequest {
    /** general / product / order */
    private String contextType;
    /** 关联的 productId 或 orderNo */
    private String contextId;
}
