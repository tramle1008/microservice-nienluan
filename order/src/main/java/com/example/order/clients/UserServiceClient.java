package com.example.order.clients;

import com.example.order.dto.ProductDTO;
import com.example.order.dto.UserDTO;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange
public interface UserServiceClient {
    @GetExchange("/api/user/{id}")
    UserDTO getUserDetail(@PathVariable Long id);
}
