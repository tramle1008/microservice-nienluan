package com.ecommerce.order_service.service;

import com.ecommerce.order_service.dto.*;
import com.ecommerce.order_service.models.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface OrderService {
    OrderDTO createOrder(Long userId, CreateOrderRequest request);
    Page<OrderDTO> getOrdersByUser(
            Long userId,
            int page,
            int size

    );
    OrderDTO getOrderDetail(Long orderId);
    OrderDTO cancelOrder(Long orderId, Long userId);
    OrderDTO updateOrderStatus(Long orderId, OrderStatus status, Long adminId); // admin

    // Trong OrderService
    void confirmCodOrder(Long orderId, Long userId);
    OrderDTO getOrderByCode(String code);
    // Giao tiếp với payment
    void markOrderPaidByCode(String orderCode, BigDecimal amount, String referenceCode);

    void markOrderSucces(Long orderId);

    void confirmByAdmin(Long orderId);


    void markDeliveredByAdmin(Long orderId);



    Page<AdminPendingOrderDTO> getAllPendingOrders(Pageable pageable);

    // Interface
    Page<AdminAllOrderDTO> getAllOrdersForAdmin(
            Pageable pageable,
            OrderStatus status,
            String search,
            String fromDate,
            String toDate
    );
    // ← THÊM MỚI: method tính tổng tiền
    BigDecimal calculateTotalFinalAmount(
            OrderStatus status,
            String search,
            LocalDate startDate,
            LocalDate endDate
    );
    List<DailyOrderCountDTO> getDailyOrderCount(LocalDate from, LocalDate to);

    // THÊM MỚI: method chính để controller gọi (có default implementation)
    default AdminOrderPage getAllOrdersWithTotalForAdmin(
            Pageable pageable,
            OrderStatus status,
            String search,
            String fromDate,
            String toDate) {

        LocalDate startDate = fromDate != null ? LocalDate.parse(fromDate) : null;
        LocalDate endDate = toDate != null ? LocalDate.parse(toDate) : null;

        Page<AdminAllOrderDTO> page = getAllOrdersForAdmin(pageable, status, search, fromDate, toDate);
        BigDecimal totalAmount = calculateTotalFinalAmount(status, search, startDate, endDate);

        return new AdminOrderPage((PageImpl<AdminAllOrderDTO>) page, totalAmount);
    }
}