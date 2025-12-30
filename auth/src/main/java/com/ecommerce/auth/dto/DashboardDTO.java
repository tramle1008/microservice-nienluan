package com.ecommerce.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardDTO {
    private long totalUsers;
    private long totalProducts;
    private long totalCategories;
    private long totalOrders;
    private BigDecimal todayRevenue;
    private BigDecimal totalRevenue = BigDecimal.ZERO;
    private long pendingOrders;
//    private List<TodayOrderDTO> todayOrders;
}
