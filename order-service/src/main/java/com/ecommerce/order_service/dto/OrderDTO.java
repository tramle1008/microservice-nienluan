package com.ecommerce.order_service.dto;
import com.ecommerce.order_service.models.Order;
import com.ecommerce.order_service.models.OrderStatus;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private Long orderId;
    private String code;
    private OrderStatus status;
    private BigDecimal totalAmount;      // tiền hàng
    private BigDecimal shippingFee;      // THÊM: phí ship
    private BigDecimal finalAmount;      // total + ship
    private String shippingAddress;
    private LocalDateTime orderDate;
    private List<OrderItemDTO> items;

    public static OrderDTO from(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setOrderId(order.getOrderId());
        dto.setCode(order.getCode());
        dto.setStatus(order.getStatus());
        dto.setTotalAmount(order.getTotalAmount());           // THÊM
        dto.setShippingFee(order.getShippingFee());           // THÊM
        dto.setFinalAmount(order.getFinalAmount());
        dto.setShippingAddress(order.getShippingAddress());
        dto.setOrderDate(order.getOrderDate());
        dto.setItems(order.getItems().stream().map(OrderItemDTO::from).toList());
        return dto;
    }
}