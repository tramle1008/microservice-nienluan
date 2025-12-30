package com.ecommerce.auth.service;

import com.ecommerce.auth.clients.OrderClient;
import com.ecommerce.auth.clients.ProductClient;
import com.ecommerce.auth.dto.DashboardDTO;
import com.ecommerce.auth.models.AppRole;
import com.ecommerce.auth.reponsitory.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminServiceIml implements AdminService {
    private final UserRepository userRepository;
    private final ProductClient productClient;
    private final OrderClient orderClient;

    @Override
    public DashboardDTO getStats() {
        DashboardDTO dto = new DashboardDTO();

        dto.setTotalUsers(userRepository.findByRoles_RoleName(AppRole.ROLE_USER).stream().count());
        dto.setTotalProducts(productClient.getTotalProducts());
        dto.setTotalCategories(productClient.getTotalCategories());
        dto.setTotalOrders(orderClient.getTotalOrders());
        dto.setTodayRevenue(orderClient.getTodayRevenue());
        dto.setTotalRevenue(orderClient.getTotalRevenue());
        dto.setPendingOrders(orderClient.countPending());
        return dto;
    }
}
