// src/main/java/com/ecommerce/product/config/SecurityConfig.java

package com.ecommerce.product.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().permitAll()
                        // 1. PUBLIC – ai cũng được
//                        .requestMatchers("/api/**/public/**").permitAll()
//
//                        // 2. ADMIN – chỉ admin quản lý sản phẩm, danh mục, discount...
//                        .requestMatchers("/api/products/auth/**").hasRole("ADMIN")
//                        .requestMatchers("/api/categories/auth/**").hasRole("ADMIN")
//                        .requestMatchers("/api/discounts/auth/**").hasRole("ADMIN")
//                        .requestMatchers("/api/variants/auth/**").hasRole("ADMIN")
//
//                        // 3. INTERNAL – chỉ các service (order-service) được gọi giảm/tăng stock
//                        .requestMatchers("/api/**/internal/**").hasRole("SERVICE")
//
//                        // 4. Các API còn lại (nếu có) cần đăng nhập
//                        .anyRequest().authenticated()
                );
        return http.build();
    }

}