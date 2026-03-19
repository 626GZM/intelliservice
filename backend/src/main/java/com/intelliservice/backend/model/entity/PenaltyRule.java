package com.intelliservice.backend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("penalty_rules")
public class PenaltyRule {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 规则编码，如 QUALITY_001 */
    private String ruleCode;

    /** 分类: quality / delivery / service / fraud */
    private String category;

    private String description;

    private Integer deductPoints;

    private BigDecimal fineAmount;

    /** minor / moderate / severe */
    private String severity;

    private LocalDateTime createdAt;
}
