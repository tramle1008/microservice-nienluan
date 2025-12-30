package com.ecommerce.payment.client;

import com.ecommerce.payment.config.AppConstants;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.ecommerce.payment.dto.OrderDTO;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderServiceClient {

    private final RestTemplate restTemplate;



    private final String orderServiceUrl = "lb://ORDER-SERVICE";

    public OrderDTO getOrderById(Long orderId) {
        String url = orderServiceUrl + "/api/orders/" + orderId;
        try {
            ResponseEntity<OrderDTO> response = restTemplate.getForEntity(url, OrderDTO.class);
            return response.getBody();
        } catch (Exception e) {
            log.warn("Không thể lấy order {}: {}", orderId, e.getMessage()); //bị lỗi Cannot resolve symbol 'log'
            return null;
        }
    }

    public OrderDTO getOrderByCode(String code) {
        String url = orderServiceUrl + "/api/orders/code/" + code;
        try {
            ResponseEntity<OrderDTO> response = restTemplate.getForEntity(url, OrderDTO.class);
            return response.getBody();
        } catch (Exception e) {
            log.warn("Không thể lấy order bằng code {}: {}", code, e.getMessage());
            return null;
        }
    }

    public void markPaid(Long orderId) {
        String url = orderServiceUrl + "/api/orders/" + orderId;
        try {
            restTemplate.put(url, null);
            log.info("Đã mark order {} là paid", orderId);
        } catch (Exception e) {
            log.error("Lỗi khi markPaid order {}: {}", orderId, e.getMessage());
        }
    }
}