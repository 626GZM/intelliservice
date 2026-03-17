package com.intelliservice.backend.model.dto;

import com.intelliservice.backend.model.entity.Message;
import com.intelliservice.backend.model.entity.Session;
import lombok.Data;

import java.util.List;

@Data
public class SessionDetailResponse {

    private Session session;
    private List<Message> messages;
}
