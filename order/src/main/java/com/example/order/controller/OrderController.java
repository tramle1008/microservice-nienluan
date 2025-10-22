package com.example.order.controller;

import com.example.order.dto.OrderResponse;
import com.example.order.service.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@AllArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(
            @RequestHeader("X-User-ID") Long userId
    ){
        Optional<OrderResponse> orderResponseOpt = orderService.createOrder(userId);

        // Nếu giỏ hàng rỗng, trả 404 hoặc 400
        if(orderResponseOpt.isEmpty()){
            return ResponseEntity.notFound().build();
        }

        // Nếu có order, trả CREATED
        return new ResponseEntity<>(orderResponseOpt.get(), HttpStatus.CREATED);
    }
}
