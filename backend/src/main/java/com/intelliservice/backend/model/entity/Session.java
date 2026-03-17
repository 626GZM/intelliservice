package com.intelliservice.backend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("sessions")
public class Session {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String title;

    /** 对应 ENUM('active','closed') */
    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
