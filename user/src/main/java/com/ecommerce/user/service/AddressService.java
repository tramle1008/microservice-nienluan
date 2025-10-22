package com.ecommerce.user.service;

import com.ecommerce.user.dto.AddressDTO;

import com.ecommerce.user.models.User;
import jakarta.validation.Valid;

import java.util.List;

public interface AddressService {
    AddressDTO createAddress(AddressDTO addressDTO, User user);

    List<AddressDTO> getAdresses();

    List<AddressDTO> getUserAddresses(User user);

    AddressDTO updateUserAddress(@Valid AddressDTO addressDTO, Long addressId);

    AddressDTO deleteUserAddress(Long addressId);
}
