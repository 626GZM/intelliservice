package com.intelliservice.backend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("tickets")
public class Ticket {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long sessionId;

    private String title;

    private String description;

    /** 对应 ENUM('low','medium','high') */
    private String priority;

    /** 对应 ENUM('open','processing','resolved','closed') */
    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime resolvedAt;
}
