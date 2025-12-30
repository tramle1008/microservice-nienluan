package com.ecommerce.order_service.client;

import com.ecommerce.order_service.dto.AddressDTO;
import com.ecommerce.order_service.dto.ShippingFeeResponse;
import com.ecommerce.order_service.dto.UserProfileDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

// AuthServiceClient.java
@Service
public class AuthServiceClient {

    @Autowired
    private RestTemplate restTemplate;

    private final String AUTH_SERVICE = "http://auth-service";

    // LẤY TOÀN BỘ THÔNG TIN USER + ĐỊA CHỈ
    public UserProfileDTO getUserProfile(Long userId) {
        String url = AUTH_SERVICE + "/api/auth/user/" + userId + "/profile";
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<UserProfileDTO> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, UserProfileDTO.class);

        return response.getBody();
    }

    // Vẫn giữ lại nếu cần riêng
    public AddressDTO getAddressById(Long addressId, Long userId) {
        String url = AUTH_SERVICE + "/api/address/" + addressId;
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Id", String.valueOf(userId));
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<AddressDTO> response = restTemplate.exchange(
                url, HttpMethod.GET, entity, AddressDTO.class);
        return response.getBody();
    }


    // Thêm vào AuthServiceClient
    public ShippingFeeResponse getShippingFee(Long provinceId) {
        String url = "http://auth-service/api/shipping/fee/province/" + provinceId;
        return restTemplate.getForObject(url, ShippingFeeResponse.class);
    }
}