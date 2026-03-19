package com.intelliservice.backend.service;

import com.intelliservice.backend.mapper.OrderMapper;
import com.intelliservice.backend.model.dto.CreateOrderRequest;
import com.intelliservice.backend.model.entity.Order;
import com.intelliservice.backend.model.entity.Product;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderMapper orderMapper;
    private final ProductService productService;

    public List<Order> getUserOrders(Long userId) {
        return orderMapper.selectByUserId(userId);
    }

    public Order getByOrderNo(String orderNo) {
        Order order = orderMapper.selectByOrderNo(orderNo);
        if (order == null) {
            throw new RuntimeException("订单不存在: " + orderNo);
        }
        return order;
    }

    public Order createOrder(Long userId, CreateOrderRequest req) {
        Product product = productService.getById(req.getProductId());

        int quantity = req.getQuantity() != null ? req.getQuantity() : 1;
        BigDecimal total = product.getPrice().multiply(BigDecimal.valueOf(quantity));

        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setProductId(product.getId());
        order.setProductName(product.getName());
        order.setMerchantId(product.getMerchantId() != null ? product.getMerchantId() : 1L);
        order.setQuantity(quantity);
        order.setTotalAmount(total);
        order.setShippingAddress(req.getShippingAddress());
        order.setStatus("paid");
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());

        orderMapper.insert(order);
        return order;
    }

    private String generateOrderNo() {
        int suffix = 100000 + new Random().nextInt(900000);
        return "ORD-" + suffix;
    }
}
