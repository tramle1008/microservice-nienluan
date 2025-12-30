package com.ecommerce.order_service.dto;

import java.time.LocalDate;

public record DailyOrderCountDTO(
        LocalDate date,
        Long count
) {
}