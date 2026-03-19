package com.intelliservice.backend.model.dto;

import lombok.Data;

@Data
public class CreateOrderRequest {
    private Long productId;
    private Integer quantity;
    private String shippingAddress;
}
