package com.ecommerce.product.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private  Long productId;
    @NotBlank
    private String productName;
    private String description;
    private Integer quantity;
    private BigDecimal discount;
    private String image;
    private BigDecimal price;
    private BigDecimal specialPrice;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
//
//    @ManyToOne
//    @JoinColumn(name = "ower_id")
//    private User user;


}
