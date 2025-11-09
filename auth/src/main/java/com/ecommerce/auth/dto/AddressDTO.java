package com.ecommerce.auth.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AddressDTO {
    private Long addressId;
    private String province; //tỉnh thành phố
    private String district; //quận huyện
    private String ward; //phường , xã, thị trấn
    private String detail;
    private String phoneNumber;

}
