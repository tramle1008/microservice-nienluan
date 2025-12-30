package com.ecommerce.order_service.controller;

import com.ecommerce.order_service.client.CartServiceClient;
import com.ecommerce.order_service.client.PaymentClient;
import com.ecommerce.order_service.client.ProductServiceClient;
import com.ecommerce.order_service.dto.*;
import com.ecommerce.order_service.models.Order;
import com.ecommerce.order_service.models.OrderStatus;
import com.ecommerce.order_service.reponsitory.OrderRepository;
import com.ecommerce.order_service.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final CartServiceClient cartServiceClient;
    private final ProductServiceClient productServiceClient;
    private final PaymentClient paymentClient;
    private final OrderRepository orderRepository;

    @PostMapping
    public ResponseEntity<OrderCreationResponse> createOrder(
            @RequestHeader("X-User-Id") Long userId,
            @RequestBody CreateOrderRequest request) {

        OrderDTO order = orderService.createOrder(userId, request);
        QRPaymentResponseDTO qrResponse = null;
        // Nếu chọn QR → gọi payment-service tạo QR
        if ("QR".equalsIgnoreCase(request.getPaymentMethod())) {
            qrResponse = paymentClient.initPayment(order.getOrderId(), userId);
        }


//        if ("COD".equalsIgnoreCase(request.getPaymentMethod())) {
//            orderService.confirmCodOrder(order.getOrderId(), userId);
//        }

        return ResponseEntity.ok(new OrderCreationResponse(order, qrResponse));
    }


//    @GetMapping
//    public ResponseEntity<List<OrderDTO>> getUserOrders(@RequestHeader("X-User-Id") Long userId) {
//        return ResponseEntity.ok(orderService.getOrdersByUser(userId));
//    }
@GetMapping
public ResponseEntity<Page<OrderDTO>> getUserOrders(
        @RequestHeader("X-User-Id") Long userId,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "2") int size,
        @RequestParam(defaultValue = "orderDate") String sortBy,
        @RequestParam(defaultValue = "desc") String sortDir
) {
    return ResponseEntity.ok(
            orderService.getOrdersByUser(userId, page, size)
    );
}



    @GetMapping("/{orderId}")
    public ResponseEntity<OrderDTO> getOrderDetail(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrderDetail(orderId));
    }

//  lấy toàn bộ đơn hàng
@GetMapping("/admin/all")
public ResponseEntity<AdminOrderPage> getAllOrdersForAdmin(
                                                             @RequestParam(defaultValue = "0") int page,
                                                             @RequestParam(defaultValue = "3") int size,
                                                             @RequestParam(defaultValue = "orderDate") String sortBy,
                                                             @RequestParam(defaultValue = "desc") String sortDir,
                                                             @RequestParam(required = false) OrderStatus status,
                                                             @RequestParam(required = false) String search,
                                                             @RequestParam(required = false) String fromDate,
                                                             @RequestParam(required = false) String toDate
) {

    Sort sort = sortDir.equalsIgnoreCase("asc")
            ? Sort.by(sortBy).ascending()
            : Sort.by(sortBy).descending();

    Pageable pageable = PageRequest.of(page, size, sort);

    // ← ĐỔI DÒNG NÀY: gọi method mới có total
    AdminOrderPage result = orderService.getAllOrdersWithTotalForAdmin(
            pageable, status, search, fromDate, toDate);

    return ResponseEntity.ok(result);
}
    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<OrderDTO> cancelOrder(@PathVariable Long orderId, @RequestHeader("userId") Long userId) {
        return ResponseEntity.ok(orderService.cancelOrder(orderId, userId));
    }

    @PutMapping("/admin/cancel/{orderId}")
    public ResponseEntity<OrderDTO> cancelOrderByAdmin(@PathVariable Long orderId, Long adminId){
        return ResponseEntity.ok(orderService.updateOrderStatus(orderId, OrderStatus.CANCELLED,adminId));
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<OrderDTO> getOrderByCode(@PathVariable String code) {
        OrderDTO order = orderService.getOrderByCode(code);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(order);
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<Void> markPaid(@PathVariable Long orderId){
        orderService.markOrderSucces(orderId);
        return  ResponseEntity.ok().build();
    }
//    mới thêm cho dashboard
    @GetMapping("/admin/count")
    public ResponseEntity<Long> countAll() {
        return ResponseEntity.ok(orderRepository.countPaidOrConfirmed());
    }
//    đếm số đơn hàng ở trạng thái PENDING
    @GetMapping("/pending/count")
    public ResponseEntity<Long> countPending() {
        return ResponseEntity.ok(orderRepository.countPendingOrders());
    }
//    liệt kế tất cả đơn hàng pending
    @GetMapping("/pending")
    public ResponseEntity<Page<AdminPendingOrderDTO>> getAllPendingOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "orderDate") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
    Sort sort = sortDir.equalsIgnoreCase("asc")
            ? Sort.by(sortBy).ascending()
            : Sort.by(sortBy).descending();
    Pageable pageable = PageRequest.of(page, size, sort);
    Page<AdminPendingOrderDTO> result = orderService.getAllPendingOrders(pageable);
    return ResponseEntity.ok(result);
}

    @GetMapping("/admin/countOrder/today")
    public ResponseEntity<Long> countTodayOrder() {
        return ResponseEntity.ok(orderRepository.countTodayOrders());
    }
    @GetMapping("/admin/revenue")
    // @PreAuthorize("hasRole('ADMIN')") // nếu có security
    public ResponseEntity<BigDecimal> getTotalRevenue() {
        BigDecimal revenue = orderRepository.getTotalRevenue();
        return ResponseEntity.ok(revenue);
    }

    @GetMapping("/admin/revenue/today")
    public ResponseEntity<BigDecimal> getTodayRevenue() {
        BigDecimal revenue = orderRepository.getTodayRevenue();
        return ResponseEntity.ok(revenue);
    }



    @GetMapping("/admin/today")
    public ResponseEntity<List<TodayOrderDTO>> getTodayOrders() {
        List<Order> orders = orderRepository.findAllTodayOrders();
        List<TodayOrderDTO> dtos = orders.stream()
                .map(order -> {
                    TodayOrderDTO dto = new TodayOrderDTO();
                    dto.setOrderId(order.getOrderId());
                    dto.setCode(order.getCode());
                    dto.setCustomerName(order.getCustomerName());
                    dto.setCustomerEmail(order.getCustomerEmail());
                    dto.setPhoneNumber(order.getPhoneNumber());
                    dto.setTotalAmount(order.getTotalAmount());
                    dto.setShippingFee(order.getShippingFee());
                    dto.setStatus(order.getStatus());
                    dto.setOrderDate(order.getOrderDate());
                    dto.setPaidAt(order.getPaidAt());
                    return dto;
                })
                .toList();
        return ResponseEntity.ok(dtos);
    }

    // Nếu muốn fetch kèm items (chi tiết sản phẩm)
    @GetMapping("/admin/today/with-items")
    public ResponseEntity<List<Order>> getTodayOrdersWithItems() {
        List<Order> todayOrders = orderRepository.findAllTodayOrdersWithItems();
        return ResponseEntity.ok(todayOrders);
    }

//    QUAN TRONG, CHINH NHANH
    @PutMapping("/{orderId}/confirm")
    public ResponseEntity<Void> confirmByAdmin(@PathVariable Long orderId) {
        orderService.confirmByAdmin(orderId);
        return ResponseEntity.noContent().build(); // 204 No Content là chuẩn cho update thành công
    }

    @PutMapping("/{orderId}/delivered")
    public ResponseEntity<Void> markDeliveredByAdmin(@PathVariable Long orderId) {
        orderService.markDeliveredByAdmin(orderId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/daily-count")
    public ResponseEntity<List<DailyOrderCountDTO>> getDailyOrderCount(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

        List<DailyOrderCountDTO> result = orderService.getDailyOrderCount(from, to);
        return ResponseEntity.ok(result);
    }
}
