package com.ecommerce.order_service.reponsitory;

import com.ecommerce.order_service.models.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // Lấy tất cả item của một order
    List<OrderItem> findByOrder_OrderId(Long orderId);

}