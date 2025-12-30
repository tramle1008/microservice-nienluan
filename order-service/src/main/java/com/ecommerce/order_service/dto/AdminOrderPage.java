package com.ecommerce.order_service.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public class AdminOrderPage extends PageImpl<AdminAllOrderDTO> {

    private final BigDecimal totalAmount;

    @JsonCreator
    public AdminOrderPage(
            @JsonProperty("content") List<AdminAllOrderDTO> content,
            @JsonProperty("pageable") Pageable pageable,
            @JsonProperty("total") long total,
            @JsonProperty("totalAmount") BigDecimal totalAmount) {
        super(content, pageable, total);
        this.totalAmount = totalAmount;
    }

    public AdminOrderPage(PageImpl<AdminAllOrderDTO> page, BigDecimal totalAmount) {
        super(page.getContent(), page.getPageable(), page.getTotalElements());
        this.totalAmount = totalAmount != null ? totalAmount : BigDecimal.ZERO;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }
}