package com.ecommerce.auth.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "province")
public class Province {
    @Id
    private Long id;
    private String name;
    @OneToOne(mappedBy = "province")
    private ShippingFeeByProvince shippingFee;

}
