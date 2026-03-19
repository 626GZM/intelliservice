package com.intelliservice.backend.controller;

import com.intelliservice.backend.model.dto.ApiResponse;
import com.intelliservice.backend.model.dto.CreateOrderRequest;
import com.intelliservice.backend.model.entity.Order;
import com.intelliservice.backend.service.OrderService;
import com.intelliservice.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;

    /** 当前用户订单列表 */
    @GetMapping
    public ApiResponse<List<Order>> list(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = currentUserId(userDetails);
        return ApiResponse.success(orderService.getUserOrders(userId));
    }

    /** 订单详情（按订单号） */
    @GetMapping("/{orderNo}")
    public ApiResponse<Order> detail(@PathVariable String orderNo) {
        return ApiResponse.success(orderService.getByOrderNo(orderNo));
    }

    /** 模拟下单 */
    @PostMapping
    public ApiResponse<Order> create(@AuthenticationPrincipal UserDetails userDetails,
                                     @RequestBody CreateOrderRequest req) {
        Long userId = currentUserId(userDetails);
        return ApiResponse.success(orderService.createOrder(userId, req));
    }

    private Long currentUserId(UserDetails userDetails) {
        return userService.getUserByUsername(userDetails.getUsername()).getId();
    }
}
