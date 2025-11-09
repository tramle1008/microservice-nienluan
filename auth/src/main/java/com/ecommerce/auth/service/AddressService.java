package com.ecommerce.auth.service;


import com.ecommerce.auth.dto.AddressDTO;
import com.ecommerce.auth.models.User;
import jakarta.validation.Valid;

import java.util.List;

public interface AddressService {
    AddressDTO createAddress(AddressDTO addressDTO, User user);

    List<AddressDTO> getAdresses();

    List<AddressDTO> getUserAddresses(User user);

    AddressDTO updateUserAddress(@Valid AddressDTO addressDTO, Long addressId);

    AddressDTO deleteUserAddress(Long addressId);
}
