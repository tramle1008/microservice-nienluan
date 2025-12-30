package com.ecommerce.auth.service;


import com.ecommerce.auth.dto.AddressDTO;
import com.ecommerce.auth.dto.ProvinceDTO;
import com.ecommerce.auth.dto.WardDTO;

import java.util.List;

public interface AddressService {
    AddressDTO addAddress(Long userId, Long provinceId, Long wardId,
                          String detail, String phone);

    AddressDTO updateAddress(Long addressId, Long provinceId, Long wardId,
                             String detail, String phone);

    void deleteAddress(Long addressId);

    List<AddressDTO> getUserAddresses(Long userId);

    List<ProvinceDTO> getProvinces();

    List<WardDTO> getWardsByProvince(Long provinceId);  // mới
}