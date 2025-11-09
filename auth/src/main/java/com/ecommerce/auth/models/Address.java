package com.ecommerce.auth.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Table(name = "address")
@ToString
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Long addressId;

    @NotBlank
    private String province; //tỉnh thành phố

    @NotBlank
    private String district; //quận huyện

    @NotBlank
    private String ward; //phường , xã, thị trấn

    @Column(name = "detail")
// Gợi ý: "Nhập số nhà, tên đường nếu có"
    private String detail;

    @NotBlank
    @ToString.Exclude
    @Size(min = 10, message = "Số điện thoại phải có ít nhất 10 chữ số")
    private String phoneNumber;


    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Address(String province, String district, String ward, String detail, String phoneNumber) {
        this.province = province;
        this.district = district;
        this.ward = ward;
        this.detail = detail;
        this.phoneNumber = phoneNumber;
    }
}
