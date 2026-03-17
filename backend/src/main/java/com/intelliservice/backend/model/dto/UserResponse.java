package com.intelliservice.backend.model.dto;

import com.intelliservice.backend.model.entity.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserResponse {

    private Long id;
    private String username;
    private String email;
    private String role;
    private LocalDateTime createdAt;

    public static UserResponse from(User user) {
        UserResponse r = new UserResponse();
        r.id = user.getId();
        r.username = user.getUsername();
        r.email = user.getEmail();
        r.role = user.getRole();
        r.createdAt = user.getCreatedAt();
        return r;
    }
}
