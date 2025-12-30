package com.ecommerce.auth.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
//dau ra
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressDTO {
    private Long addressId;
    private Long provinceId;
    private String province;
    private String ward;
    private String detail;
    private String phoneNumber;
}