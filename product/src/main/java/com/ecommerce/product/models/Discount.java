package com.ecommerce.product.models;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Discount {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long discountId;
    @NotNull
    private String name; // "Sale hè 30%", "Flash Sale"
    @NotNull
    @Min(0)
    private BigDecimal percentage; // 10.00 → 10%
    private BigDecimal maxAmount; // Giới hạn giảm tối đa (VD: 500,000)
    @NotNull
    private LocalDateTime startDate; //ngày bắt đầu
    @NotNull
    private LocalDateTime endDate; //ngày kết thúc
    private boolean active = true;
    // Có thể áp dụng cho: product, variant, category, toàn bộ
    @Enumerated(EnumType.STRING)
    private DiscountTarget target; // PRODUCT, VARIANT, CATEGORY, GLOBAL
    @Enumerated(EnumType.STRING)
    private DiscountType type; // PERCENTAGE, FIXED_AMOUNT


    // Discount.java
    @Transient // Không lưu DB
    public boolean isWithinDateRange() {
        LocalDateTime now = LocalDateTime.now();
        return (startDate == null || !now.isBefore(startDate)) &&
                (endDate == null || !now.isAfter(endDate));
    }

}