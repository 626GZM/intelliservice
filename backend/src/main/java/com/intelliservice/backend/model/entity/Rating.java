package com.intelliservice.backend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("ratings")
public class Rating {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long sessionId;

    private Long userId;

    /** good / neutral / bad */
    private String rating;

    private String comment;

    /** 是否超时自动好评 */
    private Integer autoRated;

    private LocalDateTime createdAt;
}
