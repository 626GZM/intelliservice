package com.intelliservice.backend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("messages")
public class Message {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long sessionId;

    /** 对应 ENUM('user','assistant','system') */
    private String role;

    private String content;

    private Integer tokenCount;

    private Integer responseTimeMs;

    private String agentName;

    private LocalDateTime createdAt;
}
