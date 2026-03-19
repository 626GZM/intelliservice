package com.intelliservice.backend.model.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

@Data
public class ReviewTicketRequest {
    private Boolean approved;
    private JsonNode finalPenalty;
}
