package com.ecommerce.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
//dau vao

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressRequestDTO {
    private Long provinceId;
    private Long wardId;
    private String detail;
    private String phoneNumber;
}