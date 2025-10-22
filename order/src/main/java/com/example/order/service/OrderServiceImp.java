package com.example.order.service;

import com.example.order.dto.OrderItemDTO;
import com.example.order.dto.OrderResponse;
import com.example.order.models.CartItem;
import com.example.order.models.Order;
import com.example.order.models.OrderItem;
import com.example.order.models.OrderStatus;
import com.example.order.repository.CartItemRepository;
import com.example.order.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderServiceImp implements OrderService {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Override
    public Optional<OrderResponse> createOrder(Long userId) {
        // 1️⃣ Lấy toàn bộ sản phẩm trong giỏ hàng theo userId
        List<CartItem> cartItems = cartItemRepository.findAllByUserId(userId);

        if (cartItems.isEmpty()) {
            return Optional.empty(); // Giỏ hàng rỗng
        }

        // 2️⃣ Tính tổng tiền đơn hàng
        BigDecimal totalAmount = cartItems.stream()
                .map(item -> {
                    BigDecimal price = item.getPrice() != null ? item.getPrice() : BigDecimal.valueOf(100);
                    return price.multiply(BigDecimal.valueOf(item.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3️⃣ Tạo đối tượng Order
        Order order = new Order();
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setStatus(OrderStatus.PENDING);

        // 4️⃣ Chuyển CartItem → OrderItem
        List<OrderItem> orderItems = cartItems.stream().map(cartItem -> {
            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(cartItem.getProductId());
            orderItem.setQuantity(cartItem.getQuantity());

            // Gán giá mặc định nếu chưa có
            BigDecimal price = cartItem.getPrice() != null ? cartItem.getPrice() : BigDecimal.valueOf(100);
            orderItem.setPrice(price);

            // Tính subTotal = price * quantity
            orderItem.setSubTotal(price.multiply(BigDecimal.valueOf(cartItem.getQuantity())));

            // Thiết lập quan hệ 2 chiều với Order
            orderItem.setOrder(order);

            return orderItem;
        }).collect(Collectors.toList());

        order.setItems(orderItems);

        // 5️⃣ Lưu đơn hàng và xóa giỏ hàng
        orderRepository.save(order);
        cartItemRepository.deleteAll(cartItems);

        // 6️⃣ Chuyển sang DTO để trả về
        OrderResponse response = new OrderResponse();
        response.setId(order.getOrderId());
        response.setTotalAmount(order.getTotalAmount());
        response.setStatus(order.getStatus());
        response.setCreateAt(order.getCreateAt());

        List<OrderItemDTO> itemDTOs = orderItems.stream()
                .map(oi -> new OrderItemDTO(
                        oi.getProductId(),
                        oi.getQuantity(),
                        oi.getPrice(),
                        oi.getSubTotal()
                ))
                .collect(Collectors.toList());

        response.setItems(itemDTOs);

        return Optional.of(response);
    }



}
