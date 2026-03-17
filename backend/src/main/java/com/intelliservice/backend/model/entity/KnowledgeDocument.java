package com.intelliservice.backend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("knowledge_documents")
public class KnowledgeDocument {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String filename;

    private String filePath;

    private Long fileSize;

    /** 对应 ENUM('processing','ready','failed') */
    private String status;

    private Integer chunkCount;

    private LocalDateTime createdAt;
}
