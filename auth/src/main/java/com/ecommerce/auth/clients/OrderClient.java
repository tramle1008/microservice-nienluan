package com.ecommerce.auth.clients;

import com.ecommerce.auth.dto.TodayOrderDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderClient {

    private final RestTemplate restTemplate;
    private static final String ORDER_SERVICE_URL = "http://ORDER-SERVICE/api/orders";

    public BigDecimal getTotalRevenue() {
        String url = ORDER_SERVICE_URL + "/admin/revenue";
        return restTemplate.getForObject(url, BigDecimal.class);
    }

    public BigDecimal getTodayRevenue() {
        String url = ORDER_SERVICE_URL + "/admin/revenue/today";
        return restTemplate.getForObject(url, BigDecimal.class);
    }

    public long getTotalOrders() {
        String url = ORDER_SERVICE_URL + "/admin/count";
        return restTemplate.getForObject(url, Long.class);
    }

    public List<TodayOrderDTO> getTodayOrders() {
        String url = ORDER_SERVICE_URL + "/api/orders/admin/today";

        ResponseEntity<List<TodayOrderDTO>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<TodayOrderDTO>>() {}
        );

        return response.getBody();
    }

    public List<TodayOrderDTO> getTodayOrdersSimple() {
        String url = ORDER_SERVICE_URL + "/api/orders/admin/today";
        TodayOrderDTO[] array = restTemplate.getForObject(url, TodayOrderDTO[].class);
        return array != null ? Arrays.asList(array) : Collections.emptyList();
    }


    public long countTodayOrders(){
        String url = ORDER_SERVICE_URL + "/admin/countOrder/today";
        return restTemplate.getForObject(url, Long.class);
    }

    public long countPending(){
        String url = ORDER_SERVICE_URL + "/pending/count";
        return restTemplate.getForObject(url, Long.class);
    }


}