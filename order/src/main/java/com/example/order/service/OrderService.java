package com.example.order.service;

import com.example.order.dto.OrderResponse;

import java.util.Optional;

public interface OrderService {
    Optional<OrderResponse> createOrder(Long userId);
}
