package com.intelliservice.backend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("merchants")
public class Merchant {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    /** 商户评分，满分100 */
    private Integer score;

    /** 累计罚款 */
    private BigDecimal totalFines;

    /** normal / warning / suspended */
    private String status;

    private LocalDateTime createdAt;
}
