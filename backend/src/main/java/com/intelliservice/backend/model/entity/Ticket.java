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

    /** 关联订单号 */
    private String orderNo;

    /** 涉诉商户ID */
    private Long merchantId;

    private String title;

    private String description;

    /** low / medium / high */
    private String priority;

    /** open / processing / resolved / closed */
    private String status;

    /** AI建议的处罚方案（JSON字符串） */
    private String aiSuggestion;

    /** 最终执行的处罚（JSON字符串） */
    private String finalPenalty;

    /** pending / ai_suggested / reviewed / executed / rejected */
    private String reviewStatus;

    /** 审阅客服ID */
    private Long reviewedBy;

    private LocalDateTime reviewedAt;

    private LocalDateTime createdAt;

    private LocalDateTime resolvedAt;
}
