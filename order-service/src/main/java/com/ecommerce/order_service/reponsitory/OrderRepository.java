package com.ecommerce.order_service.reponsitory;

import com.ecommerce.order_service.dto.AdminPendingOrderDTO;
import com.ecommerce.order_service.dto.DailyOrderCountDTO;
import com.ecommerce.order_service.models.Order;
import com.ecommerce.order_service.models.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // Lấy danh sách đơn hàng của user, sắp xếp mới nhất trước
    Page<Order>findByUserIdOrderByOrderDateDesc(Long userId, Pageable pageable);

    // Lấy đơn hàng + items (eager fetch)
    @EntityGraph(attributePaths = "items")
    @Query("SELECT o FROM Order o LEFT JOIN FETCH o.items WHERE o.orderId = :id")
    Optional<Order> findByIdWithItems(@Param("id") Long id);
    // Tìm đơn hàng theo userId và status
    List<Order> findByUserIdAndStatus(Long userId, OrderStatus status);

    Optional<Order> findByCode(String code);

    // 1. Tổng revenue tất cả đơn hàng đã thanh toán (hoàn thành)
    @Query("SELECT COALESCE(SUM(o.totalAmount + o.shippingFee), 0) " +
            "FROM Order o " +
            "WHERE o.status = 'PAID' OR o.status = 'DELIVERED'")
    BigDecimal getTotalRevenue();

    // 2. Tổng revenue hôm nay
    @Query("SELECT COALESCE(SUM(o.totalAmount + o.shippingFee), 0) " +
            "FROM Order o " +
            "WHERE o.status IN ('PAID', 'DELIVERED') " +
            "AND DATE(o.paidAt) = CURRENT_DATE")
    BigDecimal getTodayRevenue();

    // 3. Tổng revenue trong khoảng thời gian (tùy chọn mở rộng sau)
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o " +
            "WHERE (o.status = 'COMPLETED' OR o.status = 'DELIVERED') " +
            "AND o.paidAt BETWEEN :start AND :end")
    BigDecimal getRevenueBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // 4. Tổng số đơn hàng đã hoàn thành (để dùng cho dashboard)
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = 'COMPLETED' OR o.status = 'DELIVERED'")
    Long countCompletedOrders();

    @Query("SELECT o FROM Order o WHERE DATE(o.orderDate) = CURRENT_DATE")
    List<Order> findAllTodayOrders();

    // Nếu muốn linh hoạt hơn: dùng paidAt (đơn đã thanh toán hôm nay)
    @Query("SELECT o FROM Order o WHERE DATE(o.paidAt) = CURRENT_DATE AND o.paidAt IS NOT NULL")
    List<Order> findAllTodayPaidOrders();

    // Nếu chỉ muốn đếm đơn đã thanh toán (có paidAt không null)
    @Query("SELECT COUNT(o) FROM Order o WHERE o.paidAt IS NOT NULL")
    Long countPaidOrders();

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status IN ('PENDING', 'PAID', 'CONFIRMED', 'SHIPPED', 'DELIVERED', 'COMPLETED')")
    Long countPaidOrConfirmed();

    @Query("SELECT COUNT(o) FROM Order o WHERE FUNCTION('DATE', o.orderDate) = CURRENT_DATE")
    Long countTodayOrders();

    @EntityGraph(attributePaths = "items")
    @Query("SELECT o FROM Order o WHERE FUNCTION('DATE', o.orderDate) = CURRENT_DATE")
    List<Order> findAllTodayOrdersWithItems();

    // Đếm số đơn hàng đang PENDING
    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = 'PENDING'")
    Long countPendingOrders();

    // Lấy danh sách đơn PENDING
    List<Order> findByStatusOrderByOrderDateDesc(OrderStatus status);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE :status IS NULL OR o.status = :status")
    Page<Order> findByStatusOrAll(@Param("status") OrderStatus status, Pageable pageable);

    // Repository
    // 1. findAllWithFilters (lấy danh sách phân trang)
    @Query("""
    SELECT o FROM Order o 
    WHERE (:status IS NULL OR o.status = :status)
      AND (:search IS NULL OR 
           CAST(o.orderId AS string) LIKE CONCAT('%', :search, '%') 
           OR LOWER(o.code) LIKE LOWER(CONCAT('%', :search, '%'))
          )
      AND (:startDateTime IS NULL OR o.orderDate >= :startDateTime)
      AND (:endDateTime IS NULL OR o.orderDate < :endDateTime)
    ORDER BY o.orderDate DESC
    """)
    Page<Order> findAllWithFilters(
            @Param("status") OrderStatus status,
            @Param("search") String search,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime,
            Pageable pageable);

    // 2. calculateTotalFinalAmount (tổng tiền) – cái này bạn viết ĐÚNG rồi, giữ nguyên
    @Query("SELECT COALESCE(SUM(o.totalAmount + o.shippingFee), 0) " +
            "FROM Order o " +
            "WHERE (:status IS NULL OR o.status = :status) " +
            "  AND (:search IS NULL OR CAST(o.orderId AS string) LIKE CONCAT('%', :search, '%')) " +
            "  AND (:startDateTime IS NULL OR o.orderDate >= :startDateTime) " +
            "  AND (:endDateTime IS NULL OR o.orderDate < :endDateTime)")
    BigDecimal calculateTotalFinalAmount(
            @Param("status") OrderStatus status,
            @Param("search") String search,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime);

    @Query("""
        SELECT new com.ecommerce.order_service.dto.DailyOrderCountDTO(
            CAST(o.orderDate AS localdate),
            COUNT(o)
        )
        FROM Order o
        WHERE CAST(o.orderDate AS localdate) BETWEEN :from AND :to
        GROUP BY CAST(o.orderDate AS localdate)
        ORDER BY CAST(o.orderDate AS localdate)
    """)
    List<DailyOrderCountDTO> countOrdersGroupByDate(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );
}