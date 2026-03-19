package com.intelliservice.backend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("orders")
public class Order {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String orderNo;

    private Long userId;

    private Long productId;

    private String productName;

    private Long merchantId;

    private Integer quantity;

    private BigDecimal totalAmount;

    private String shippingAddress;

    /** pending / paid / shipped / delivered / cancelled */
    private String status;

    private String courier;

    private String courierNo;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
