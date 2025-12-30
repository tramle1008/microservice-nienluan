package com.ecommerce.auth.models;


import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "shipping_fee_by_province")
@Data
@NoArgsConstructor
public class ShippingFeeByProvince {
    @Id
    private Long provinceId;
    @OneToOne
    @MapsId
    @JoinColumn(name = "province_id")
    private Province province;

    @Column(nullable = false)
    private BigDecimal defaultFee = BigDecimal.valueOf(500);

    @Column(nullable = false)
    private BigDecimal freeShippingThreshold = BigDecimal.valueOf(500);
}