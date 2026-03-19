package com.intelliservice.backend.model.dto;

import lombok.Data;

@Data
public class SubmitRatingRequest {
    private Long sessionId;
    /** good / neutral / bad */
    private String rating;
    private String comment;
}
