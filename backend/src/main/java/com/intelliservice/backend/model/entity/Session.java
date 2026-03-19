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

    /** active / closed */
    private String status;

    /** general / product / order */
    private String contextType;

    /** 关联的 productId 或 orderNo */
    private String contextId;

    /** ai / waiting / serving / closed */
    private String transferStatus;

    /** 接手的客服ID */
    private Long agentId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
