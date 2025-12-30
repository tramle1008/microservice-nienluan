package com.ecommerce.order_service.service;

import com.ecommerce.order_service.client.AuthServiceClient;
import com.ecommerce.order_service.client.CartServiceClient;
import com.ecommerce.order_service.client.ProductServiceClient;
import com.ecommerce.order_service.dto.*;
import com.ecommerce.order_service.exceptions.BadRequestException;
import com.ecommerce.order_service.exceptions.ResourceNotFoundException;
import com.ecommerce.order_service.exceptions.UnauthorizedException;
import com.ecommerce.order_service.models.Order;
import com.ecommerce.order_service.models.OrderItem;
import com.ecommerce.order_service.models.OrderMethod;
import com.ecommerce.order_service.models.OrderStatus;
import com.ecommerce.order_service.reponsitory.OrderItemRepository;
import com.ecommerce.order_service.reponsitory.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository; // ← THÊM NÀY
    private final CartServiceClient cartClient;
    private final ProductServiceClient productClient;
    private final AuthServiceClient authClient;

    @Override
    @Transactional
    public OrderDTO createOrder(Long userId, CreateOrderRequest request) {

        // 1. LẤY GIỎ HÀNG – KIỂM TRA RỖNG
        CartDTO cart = cartClient.getCurrentCart(userId);
        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new BadRequestException("Giỏ hàng trống, không thể tạo đơn hàng");
        }

        // 2. LẤY THÔNG TIN USER + ĐỊA CHỈ GIAO HÀNG
        UserProfileDTO profile = authClient.getUserProfile(userId);

        AddressDTO selectedAddress = profile.getAddresses().stream()
                .filter(a -> a.getAddressId().equals(request.getAddressId()))
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Địa chỉ giao hàng không tồn tại hoặc không thuộc về bạn"));

        Long provinceId = selectedAddress.getProvinceId();
        if (provinceId == null) {
            throw new BadRequestException("Địa chỉ không có thông tin tỉnh/thành phố");
        }

        // 3. LẤY CẤU HÌNH PHÍ SHIP THEO TỈNH
        ShippingFeeResponse feeConfig = authClient.getShippingFee(provinceId);

        // ===================================================================
        // 4. TẠO ĐƠN HÀNG TRƯỚC (QUAN TRỌNG NHẤT – ĐỂ OrderItem CÓ THỂ SET ORDER)
        // ===================================================================
        Order order = new Order();
        order.setUserId(userId);
        order.setCode(generateOrderCode());
        if ("QR".equalsIgnoreCase(request.getPaymentMethod())) {
            order.setStatus(OrderStatus.PENDING_PAYMENT);
            order.setMethod(OrderMethod.QR);
        } else if ("COD".equalsIgnoreCase(request.getPaymentMethod())) {
            order.setStatus(OrderStatus.PENDING); // sẽ confirm sau
            order.setMethod(OrderMethod.COD);
        } else {
            throw new BadRequestException("Phương thức thanh toán không hỗ trợ");
        }
        order.setCustomerName(profile.getUsername());
        order.setCustomerEmail(profile.getEmail());
        order.setPhoneNumber(selectedAddress.getPhoneNumber());
        order.setShippingAddress(formatAddress(selectedAddress));
        order.setOrderDate(LocalDateTime.now());

        // Chuẩn bị list OrderItem và tổng tiền hàng
        BigDecimal itemsTotal = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        // ===================================================================
        // 5. DUYỆT TỪNG ITEM TRONG GIỎ → TẠO OrderItem + TÍNH TỔNG
        // ===================================================================
        for (CartItemDTO item : cart.getItems()) {

            ProductVariantDTO variant = productClient.getVariant(item.getVariantId());

            // Kiểm tra tồn kho (chỉ kiểm tra, chưa khóa – sẽ khóa thật khi thanh toán)
            if(variant.getStockQuantity() == 0) {
                throw new BadRequestException("Sản phẩm " + variant.getProductName() + " " + variant.getColor() + " tạm hết hàn");
            }

            if (variant.getStockQuantity() < item.getQuantity()) {
                throw new BadRequestException(
                        "Sản phẩm " + variant.getProductName() + " " + variant.getColor()+ " chỉ còn" + variant.getStockQuantity() + " sản phẩm vui lòng vào giỏ hàng chỉnh lại số lượng");
            }
            productClient.reduceStock(item.getVariantId(), item.getQuantity());

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);  // BÂY GIỜ ĐÃ CÓ order → KHÔNG BỊ LỖI NỮA!
            orderItem.setProductId(item.getProductId());
            orderItem.setProductName(item.getProductName());
            orderItem.setVariantId(item.getVariantId());
            orderItem.setVariantColor(variant.getColor());
            orderItem.setImageUrl(item.getImageUrl());
            orderItem.setQuantity(item.getQuantity());
            orderItem.setUnitPrice(variant.getFinalPrice());
            orderItem.setSubtotal(variant.getFinalPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            orderItem.setDiscountId(variant.getAppliedProductDiscounts().getFirst().getDiscountId());
            orderItems.add(orderItem);
            itemsTotal = itemsTotal.add(orderItem.getSubtotal());
        }

        // ===================================================================
        // 6. TÍNH PHÍ SHIP CUỐI CÙNG
        // ===================================================================
        BigDecimal shippingFee = itemsTotal.compareTo(feeConfig.getFreeShippingThreshold()) >= 0
                ? BigDecimal.ZERO
                : feeConfig.getDefaultFee(); // có thể thêm innerCityFee sau này

        // ===================================================================
        // 7. GÁN DỮ LIỆU CUỐI CÙNG VÀO ORDER
        // ===================================================================
        order.setTotalAmount(itemsTotal);           // tiền hàng
        order.setShippingFee(shippingFee);          // phí ship
        order.setItems(orderItems);                 // gắn danh sách item

        // ===================================================================
        // 8. LƯU ĐƠN HÀNG (cascade → tự lưu OrderItem)
        // ===================================================================
        orderRepository.save(order);

        // ===================================================================
        // 9. XÓA GIỎ HÀNG SAU KHI ĐẶT THÀNH CÔNG
        // ===================================================================
        cartClient.clearCart(userId);

        // ===================================================================
        // 10. TRẢ VỀ KẾT QUẢ
        // ===================================================================
        return OrderDTO.from(order);
    }

    @Transactional
    public void confirmCodOrder(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn"));

        if (!order.getUserId().equals(userId)) {
            throw new UnauthorizedException("Không phải đơn của bạn");
        }

        order.setStatus(OrderStatus.CONFIRMED);
        orderRepository.save(order);
    }

    @Override
    public Page<OrderDTO> getOrdersByUser(
            Long userId,
            int page,
            int size

    ) {


        Pageable pageable = PageRequest.of(page, size);

        Page<Order> orders = orderRepository.findByUserIdOrderByOrderDateDesc(userId, pageable);

        return orders.map(OrderDTO::from);
    }

    @Override
    public Page<AdminAllOrderDTO> getAllOrdersForAdmin(
            Pageable pageable,
            OrderStatus status,
            String search,
            String fromDate,
            String toDate) {

        // Service
        LocalDate localStartDate = fromDate != null ? LocalDate.parse(fromDate) : null;
        LocalDate localEndDate = toDate != null ? LocalDate.parse(toDate) : null;

        // ← THÊM PHẦN CONVERT NÀY
        LocalDateTime startDateTime = localStartDate != null ? localStartDate.atStartOfDay() : null;
        LocalDateTime endDateTime = localEndDate != null ? localEndDate.plusDays(1).atStartOfDay() : null;

        Page<Order> page = orderRepository.findAllWithFilters(status, search, startDateTime, endDateTime, pageable);

        return page.map(order -> new AdminAllOrderDTO(
                order.getOrderId(),
                order.getCode(),
                order.getCustomerName(),
                order.getCustomerEmail(),
                order.getPhoneNumber(),
                order.getShippingAddress(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getShippingFee(),
                order.getTotalAmount().add(order.getShippingFee()),
                order.getOrderDate(),
                order.getPaidAt(),
                order.getShippedAt(),
                order.getDeliveredAt(),
                order.getCancelledAt()
        ));
    }
    @Override
    public BigDecimal calculateTotalFinalAmount(
            OrderStatus status,
            String search,
            LocalDate startDate,   // ← vẫn nhận LocalDate từ interface
            LocalDate endDate) {

        // Convert giống hệt như trên
        LocalDateTime startDateTime = startDate != null ? startDate.atStartOfDay() : null;
        LocalDateTime endDateTime = endDate != null ? endDate.plusDays(1).atStartOfDay() : null;

        return orderRepository.calculateTotalFinalAmount(status, search, startDateTime, endDateTime);
    }

    @Override
    public OrderDTO getOrderDetail(Long orderId) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));
        return OrderDTO.from(order);
    }

    @Override
    public OrderDTO cancelOrder(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));

        if (!order.getUserId().equals(userId)) {
            throw new UnauthorizedException("Không phải đơn hàng của bạn");
        }

        if (!Set.of(OrderStatus.PENDING, OrderStatus.CONFIRMED).contains(order.getStatus())) {
            throw new BadRequestException("Không thể hủy đơn hàng đã thanh toán hoặc đang giao");
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledAt(LocalDateTime.now());

        // Hoàn lại stock
        for (OrderItem item : order.getItems()) {
            productClient.increaseStock(item.getVariantId(), item.getQuantity());
        }

        orderRepository.save(order);
        return OrderDTO.from(order);
    }

    @Override
    public OrderDTO updateOrderStatus(Long orderId, OrderStatus status, Long adminId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));

        if (!hasRole(adminId, "ROLE_ADMIN")) {
            throw new UnauthorizedException("Không có quyền");
        }

        order.setStatus(status);
        switch (status) {
            case CANCELLED -> order.setCancelledAt(LocalDateTime.now());
            case PAID -> order.setPaidAt(LocalDateTime.now());
            case SHIPPED -> order.setShippedAt(LocalDateTime.now());
            case DELIVERED -> order.setDeliveredAt(LocalDateTime.now());
        }
        return OrderDTO.from(orderRepository.save(order));
    }

    private String formatAddress(AddressDTO a) {
        return String.format("%s, %s, %s", a.getDetail(), a.getWard(), a.getProvince());
    }

    private boolean hasRole(Long userId, String role) {
        // Gọi auth-service để kiểm tra role
        // Ví dụ: authClient.hasRole(userId, role)
        return true;
    }
    private String generateOrderCode() {
        return "DH" + System.currentTimeMillis();
    }
    @Override
    public OrderDTO getOrderByCode(String code) {
        Order order = orderRepository.findByCode(code)
                .orElse(null);
        return order != null ? OrderDTO.from(order) : null;
    }


    @Transactional
    public void markOrderPaidByCode(String orderCode, BigDecimal amount, String referenceCode) {
        Order order = orderRepository.findByCode(orderCode)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn: " + orderCode));

        if (order.getStatus() == OrderStatus.PAID || order.getStatus() == OrderStatus.CONFIRMED) {
            return; // đã xử lý rồi
        }

        if (order.getFinalAmount().compareTo(amount) != 0) {
            throw new BadRequestException("Số tiền không khớp");
        }


        for (OrderItem item : order.getItems()) {
            productClient.reduceStock(item.getVariantId(), item.getQuantity());
        }

        order.setStatus(OrderStatus.PAID);
        order.setPaidAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    @Override
    public void markOrderSucces(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không Tìm Thấy Đơn Haàng"));
        for (OrderItem item : order.getItems()) {
            productClient.reduceStock(item.getVariantId(), item.getQuantity());
        }
        order.setStatus(OrderStatus.PAID);
        order.setPaidAt(LocalDateTime.now());
        orderRepository.save(order);
    }

    @Override
    public void confirmByAdmin(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Không Tìm Thấy Đơn Haàng"));
        order.setStatus(OrderStatus.CONFIRMED);
        order.setShippedAt(LocalDateTime.now());
        orderRepository.save(order);
    }


    @Override
    public void markDeliveredByAdmin(Long orderId) {
        Order order = findOrderOrThrow(orderId);
        order.setStatus(OrderStatus.DELIVERED);
        order.setDeliveredAt(LocalDateTime.now());
        order.setPaidAt(LocalDateTime.now());
        orderRepository.save(order);
    }
//    @Override
//    public void markPaidByAdmin(Long orderId) {
//        Order order = orderRepository.findById(orderId)
//                .orElseThrow(() -> new RuntimeException("Không Tìm Thấy Đơn Haàng"));
//        order.setStatus(OrderStatus.CONFIRMED);
//        orderRepository.save(order);
//    }



    @Override
    public Page<AdminPendingOrderDTO> getAllPendingOrders(Pageable pageable) {
        // Lấy Page<Order> từ repository, có phân trang + sắp xếp
        Page<Order> orderPage = orderRepository.findByStatus(OrderStatus.PENDING, pageable);

        // Map từng Order sang AdminPendingOrderDTO
        return orderPage.map(order -> new AdminPendingOrderDTO(
                order.getOrderId(),
                order.getCode(),
                order.getUserId(),
                order.getCustomerName(),
                order.getCustomerEmail(),
                order.getPhoneNumber(),
                order.getShippingAddress(),
                order.getStatus().name(),

                order.getTotalAmount(),
                order.getShippingFee(),

                order.getOrderDate(),
                order.getPaidAt(),
                order.getShippedAt(),
                order.getDeliveredAt(),
                order.getCancelledAt(),

                order.getPaymentId(),
                order.getMethod()
        ));
    }

    private Order findOrderOrThrow(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng: " + orderId));
    }
    @Override
    public List<DailyOrderCountDTO> getDailyOrderCount(LocalDate from, LocalDate to) {
        // Nếu không truyền, mặc định lấy 30 ngày gần nhất
        if (from == null) {
            from = LocalDate.now().minusDays(30);
        }
        if (to == null) {
            to = LocalDate.now();
        }

        // Đảm bảo from <= to
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("From date must be before or equal to To date");
        }

        return orderRepository.countOrdersGroupByDate(from, to);
    }

}